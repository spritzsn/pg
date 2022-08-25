//package io.github.spritzsn.pg
//
//import io.github.edadma.libpq.Oid
//import io.github.spritzsn.async.*
//import io.github.spritzsn.libuv.defaultLoop
//
//import scala.util.{Failure, Success}
//
//// dbname=postgres user=postgres password=docker
//
//@main def run(): Unit =
//  query("dbname=sc user=sc password=sc port=5442 host=localhost", "select * from eta") onComplete {
//    case Success(value)     => println(value)
//    case Failure(exception) => println(s"exception: ${exception.getMessage}")
//  }
//  defaultLoop.run()
