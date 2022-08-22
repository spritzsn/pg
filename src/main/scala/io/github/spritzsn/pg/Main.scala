//package io.github.spritzsn.pg
//
//import io.github.spritzsn.async.*
//import io.github.spritzsn.libuv.defaultLoop
//
//import scala.util.{Failure, Success}
//
//@main def run(): Unit =
//  query("select * from cars") onComplete {
//    case Success(value)     => println(value)
//    case Failure(exception) => println(exception.getMessage)
//  }
//  defaultLoop.run()
