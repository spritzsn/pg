package io.github.spritzsn.pg

import io.github.edadma.libpq.Oid
import io.github.edadma.table.TextTable

import scala.collection.immutable.ArraySeq
import scala.language.dynamics

class Result(val columns: ArraySeq[String], val types: ArraySeq[Oid], val data: ArraySeq[ArraySeq[Any]]):
  private val columnMap = columns.zipWithIndex.toMap

  class Value private[Result] (v: Any):
    def toInt: Int = v.asInstanceOf[Int]
    def toDouble: Double = v.asInstanceOf[Double]

    override def toString: String = v.toString

  class Row private[Result] (idx: Int) extends Dynamic:
    def selectDynamic(field: String): Value =
      columnMap get field match
        case Some(col) => new Value(data(idx)(col))
        case None      => sys.error(s"field '$field' not found")

  def apply(idx: Int): Row =
    require(0 <= idx && idx < data.length, s"row index out of range: $idx")
    new Row(idx)

  override def toString: String =
    import Oid.*

    val table =
      new TextTable():
        headerSeq(columns)
        for r <- data do rowSeq(r)

        for (t, i) <- types.zipWithIndex do
          t match
            case INT2OID | INT4OID | INT8OID | FLOAT4OID | FLOAT8OID | NUMERICOID => rightAlignment(i + 1)
            case _                                                                =>

    table.toString
