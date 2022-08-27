package io.github.spritzsn.pg

import io.github.edadma.libpq.{ConnStatus, Connection, Oid, connectDB}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

import io.github.spritzsn.async.*

class Pool(connInfo: String, maxConnections: Int = 10):
  private val available = new mutable.Queue[Connection]
  private val unavailable = new mutable.Queue[Connection]

  enqueue

  def connection: Future[Connection] =
    if available.isEmpty then
      val conn = available.dequeue

      unavailable enqueue conn
      Future(conn)
    else if unavailable.length < maxConnections then Future(enqueue)
    else Future.unit flatMap (_ => connection)

  private def enqueue: Connection =
    connect(connInfo) match
      case Right(conn) =>
        available enqueue conn
        conn
      case Left(error) => sys.error(s"connection failed: $error")

  def query(sql: String): Future[Result] = connection flatMap (conn => io.github.spritzsn.pg.query(conn, sql))
