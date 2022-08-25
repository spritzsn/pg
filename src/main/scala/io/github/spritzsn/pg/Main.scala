//package io.github.spritzsn.pg
//
//import io.github.spritzsn.async.*
//import io.github.spritzsn.libuv.defaultLoop
//
//import scala.util.{Failure, Success}
//
//@main def run(): Unit =
//  query("dbname=postgres user=postgres password=docker host=localhost", "selectasdf * from cars") onComplete {
//    case Success(value)     => println(value)
//    case Failure(exception) => println(s"exception: ${exception.getMessage}")
//  }
//  defaultLoop.run()
