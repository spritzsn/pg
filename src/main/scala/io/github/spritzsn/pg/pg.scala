package io.github.spritzsn.pg

import scala.collection.immutable.ArraySeq
import scala.concurrent.{Future, Promise}
import io.github.edadma.libpq
import io.github.edadma.libpq.{connectDB, Oid, ConnStatus}
import io.github.spritzsn.libuv.*

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

def query(conninfo: String, sql: String): Future[Result] =
  val promise = Promise[Result]()
  val conn = connectDB(conninfo)

  def error(msg: String): Unit =
    conn.finish()
    promise.failure(new RuntimeException(msg))

  if conn.status == ConnStatus.BAD then error(s"connection failed: ${conn.errorMessage}")
  else
    val socket = conn.socket

    if socket < 0 then error(s"bad socket: $socket: ${conn.errorMessage}")
    else
      val sendres = conn.sendQuery(sql)

      if !sendres then error(s"sendQuery() failed: ${conn.errorMessage}")
      else
        val buf = new ArrayBuffer[ArraySeq[Any]]
        var columns: ArraySeq[String] = null
        var types: ArraySeq[Oid] = null
        val poll = defaultLoop.poll(socket)

        def pollCallback(poll: Poll, status: Int, events: Int): Unit =
          poll.stop
          poll.dispose()

          if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")
          else
            var consumeres: Boolean = true

            while conn.isBusy && consumeres do consumeres = conn.consumeInput

            if !consumeres then error(s"consumeInput() failed: ${conn.errorMessage}")
            else if conn.errorMessage.nonEmpty then error(conn.errorMessage)
            else
              @tailrec
              def results(): Unit =
                val res = conn.getResult

                if !res.isNull then
                  val rows = res.nTuples
                  val cols = res.nFields

                  if columns == null then
                    columns = (for i <- 0 until cols yield res.fName(i)) to ArraySeq
                    types = (for i <- 0 until cols yield res.fType(i)) to ArraySeq

                  for i <- 0 until rows do buf += (for j <- 0 until cols yield value(res, i, j)) to ArraySeq

                  res.clear()
                  results()
              end results

              results()
              conn.finish()
              promise.success(Result(columns, types, buf to ArraySeq))
            end if
          end if
        end pollCallback

        poll.start(UV_READABLE, pollCallback)
      end if
    end if
  end if

  promise.future

def value(res: libpq.Result, row: Int, col: Int): Any =
  import Oid.*

  if res.getIsNull(row, col) then null
  else
    val v = res.getValue(row, col)

    res.fType(col) match
      case BOOLOID               => v == "t"
      case INT2OID | INT4OID     => v.toInt
      case INT8OID               => v.toLong
      case NUMERICOID            => BigDecimal(v)
      case FLOAT4OID | FLOAT8OID => v.toDouble
      case _                     => v

case class Result(columns: ArraySeq[String], types: ArraySeq[Oid], data: ArraySeq[ArraySeq[Any]])
