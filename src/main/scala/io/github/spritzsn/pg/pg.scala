package io.github.spritzsn.pg

import scala.collection.immutable.ArraySeq
import scala.concurrent.Future

def query(sql: String): Future[Result] = null

class Result(val columns: ArraySeq[String], val data: ArraySeq[ArraySeq[Any]])
