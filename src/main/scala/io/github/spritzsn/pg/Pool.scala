package io.github.spritzsn.pg

import io.github.edadma.libpq.{ConnStatus, Connection, Oid, connectDB}

import scala.collection.mutable
import scala.concurrent.{Future, Promise}

import io.github.spritzsn.async.*

class Pool(connInfo: String, maxConnections: Int = 10):
  private val available = new mutable.Queue[Connection]
  private var used = 0

  enqueue

  def close(): Unit =
    if used > 0 then Console.err.println("Pool.close: there are connections still in use that will not be closed")
    available foreach (_.finish())
    available.clear()

  def connection: Future[Connection] =
    if available.nonEmpty then
      used += 1
      Future(available.dequeue)
    else if used < maxConnections then Future(enqueue)
    else Future.unit flatMap (_ => connection)

  private def enqueue: Connection =
    connect(connInfo) match
      case Right(conn) =>
        available enqueue conn
        conn
      case Left(error) => sys.error(s"connection failed: $error")

  def query(sql: String): Future[Result] =
    connection flatMap (conn =>
      io.github.spritzsn.pg.query(conn, sql) andThen { _ =>
        available enqueue conn
        used -= 1
      }
    )
