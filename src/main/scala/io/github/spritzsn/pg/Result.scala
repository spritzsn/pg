package io.github.spritzsn.pg

import io.github.edadma.libpq.Oid
import io.github.edadma.table.TextTable

import scala.collection.immutable.ArraySeq
import scala.language.dynamics

class Result(val columns: ArraySeq[String], val types: ArraySeq[Oid], val data: ArraySeq[ArraySeq[Any]])
    extends Dynamic:
  private val columnMap = columns.zipWithIndex.toMap

  class Value private[Result] (name: String, row: Int):
    private val v =
      columnMap get name match
        case Some(col) => data(row)(col)
        case None      => sys.error(s"field '$name' not found")

    def toInt: Int = v.asInstanceOf[Int]

    def toDouble: Double = v.asInstanceOf[Double]

    override def toString: String = v.toString

  class Row private[Result] (idx: Int) extends Dynamic:
    def selectDynamic(field: String): Value = new Value(field, idx)

  def selectDynamic(field: String): Value =
    require(data.length == 1, s"result must contain exactly one row: ${data.length} row(s)")
    new Value(field, 0)

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
