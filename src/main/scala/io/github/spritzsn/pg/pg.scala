package io.github.spritzsn.pg

import scala.collection.immutable.ArraySeq
import scala.concurrent.Future
import io.github.edadma.libpq.*

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

def query(sql: String): Result =
  val conn = connectdb("dbname=postgres user=postgres password=docker host=localhost")

  def error(msg: String): Nothing =
    conn.finish()
    sys.error(msg)

  if conn.status == ConnStatus.BAD then error(s"connectdb() failed: ${conn.errorMessage}")

  println(s"connected: $conn")

  val socket = conn.socket

  if socket < 0 then error(s"bad socket: $socket: ${conn.errorMessage}")

  val sendres = conn.sendQuery(sql)

  if !sendres then error(s"sendQuery() failed: ${conn.errorMessage}")

  val buf = new ArrayBuffer[ArraySeq[Any]]
  var columns: ArraySeq[String] = null

  val pollfd = stackalloc[struct_pollfd]()

  pollfd.fd = socket
  pollfd.events = POLLIN

  println("polling")

  val pollres = poll(pollfd, 1.toULong, 1000)

  if pollres == 0 then error("timed out")
  if pollres < 0 then error(s"polling error: $pollres")

  println(s"poll result: $pollres")

  if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")

  while conn.isBusy do if !conn.consumeInput then error(s"consumeInput() failed: ${conn.errorMessage}")

  @tailrec
  def results(): Unit =
    println("getResult")

    val res = conn.getResult

    if !res.isNull then
      val rows = res.ntuples
      val cols = res.nfields

      if columns == null then columns = (for i <- 0 until cols yield res.fname(i)) to ArraySeq
      for i <- 0 until rows do buf += (for j <- 0 until cols yield res.getvalue(i, j)) to ArraySeq

      res.clear()
      results()

  results()
  conn.finish()
  new Result(columns, buf to ArraySeq)

class Result(val columns: ArraySeq[String], val data: ArraySeq[ArraySeq[Any]]):
  override def toString: String =
    columns.toString ++ "\n" ++ data.mkString("\n")
