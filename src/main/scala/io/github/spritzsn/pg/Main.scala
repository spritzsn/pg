//package io.github.spritzsn.pg
//
//import io.github.edadma.libpq.Oid
//import io.github.spritzsn.async.*
//import io.github.spritzsn.libuv.defaultLoop
//
//import scala.util.{Failure, Success}
//
//@main def run(): Unit =
//  query("dbname=postgres user=postgres password=docker host=localhost", "select * from rep limit 1") onComplete {
//    case Success(value)     => println(value.name)
//    case Failure(exception) => println(s"exception: ${exception.getMessage}")
//  }
//  defaultLoop.run()
