package io.github.spritzsn.pg

@main def run(): Unit =
  println(query("select * from cars"))
