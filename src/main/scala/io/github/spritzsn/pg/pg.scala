package io.github.spritzsn.pg

import scala.collection.immutable.ArraySeq
import scala.concurrent.{Future, Promise}
import io.github.edadma.libpq.*
import io.github.spritzsn.libuv.*

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

def query(sql: String): Future[Result] =
  val promise = Promise[Result]()
  val conn = connectdb("dbname=postgres user=postgres password=docker host=localhost")

  def error(msg: String): Nothing =
    conn.finish()
    sys.error(msg)

  if conn.status == ConnStatus.BAD then error(s"connectdb() failed: ${conn.errorMessage}")

  val socket = conn.socket

  if socket < 0 then error(s"bad socket: $socket: ${conn.errorMessage}")

  val sendres = conn.sendQuery(sql)

  if !sendres then error(s"sendQuery() failed: ${conn.errorMessage}")

  val buf = new ArrayBuffer[ArraySeq[Any]]
  var columns: ArraySeq[String] = null
  val poll = defaultLoop.poll(socket)

  def pollCallback(poll: Poll, status: Int, events: Int): Unit =
    if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")

    while conn.isBusy do if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")

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

    results()
    poll.stop
    poll.dispose()
    conn.finish()
    promise.success(new Result(columns, buf to ArraySeq))

  poll.start(UV_READABLE, pollCallback)
  promise.future

class Result(val columns: ArraySeq[String], val data: ArraySeq[ArraySeq[Any]]):
  override def toString: String =
    columns.toString ++ "\n" ++ data.mkString("\n")