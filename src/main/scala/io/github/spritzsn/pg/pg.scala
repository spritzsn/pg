package io.github.spritzsn.pg

import scala.collection.immutable.ArraySeq
import scala.concurrent.{Future, Promise}
import io.github.edadma.libpq.*
import io.github.spritzsn.libuv.*

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

def query(conninfo: String, sql: String): Future[Result] =
  val promise = Promise[Result]()
  val conn = connectdb(conninfo)

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
        val poll = defaultLoop.poll(socket)

        def pollCallback(poll: Poll, status: Int, events: Int): Unit =
          poll.stop
          poll.dispose()

          if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")
          else
            var consumeres: Boolean = true

            while conn.isBusy && consumeres do consumeres = conn.consumeInput

            if !consumeres then error(s"consumeInput() failed: ${conn.errorMessage}")
            else
              @tailrec
              def results(): Unit =
                val res = conn.getResult

                if !res.isNull then
                  val rows = res.ntuples
                  val cols = res.nfields

                  if columns == null then columns = (for i <- 0 until cols yield res.fname(i)) to ArraySeq
                  for i <- 0 until rows do buf += (for j <- 0 until cols yield res.getvalue(i, j)) to ArraySeq

                  res.clear()
                  results()
              end results

              results()
              conn.finish()
              promise.success(Result(columns, buf to ArraySeq))
            end if
          end if
        end pollCallback

        poll.start(UV_READABLE, pollCallback)
      end if
    end if
  end if

  promise.future

case class Result(columns: ArraySeq[String], data: ArraySeq[ArraySeq[Any]])
