//package io.github.spritzsn.pg
//
//import io.github.edadma.libpq.Oid
//import io.github.spritzsn.async.*
//import io.github.spritzsn.libuv.defaultLoop
//
//import scala.util.{Failure, Success}
//
//@main def run(): Unit =
//  val pool = new Pool("dbname=postgres user=postgres password=docker host=localhost")
//
//  pool.query("select * from cars") andThen (_ => pool.close()) onComplete {
//    case Success(value)     => println(value)
//    case Failure(exception) => println(s"exception: ${exception.getMessage}")
//  }
//  defaultLoop.run()
