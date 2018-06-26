package parser

import scala.annotation.meta.field
import scala.scalajs.js.annotation.{JSExport, JSExportAll, JSExportDescendentObjects, JSExportTopLevel}

object AST {
  @JSExportDescendentObjects
  sealed trait Clause extends Product with Serializable {
    @JSExport
    def start: Int
    @JSExport
    def end: Int
  }

  sealed trait Predicate extends Clause

  @JSExportDescendentObjects
  sealed trait BooleanPredicate extends Predicate {
    @JSExport
    def left: Predicate

    @JSExport
    def right: Predicate
  }
  final case class Key(@(JSExport @field)key: String, start: Int, end: Int) extends Clause
  final case class Value(@(JSExport @field)value: Comparison, start: Int, end: Int) extends Clause
  final case class KeyValue(@(JSExport @field)key: Key, @(JSExport @field)value: Value, start: Int, end: Int) extends Predicate
  final case class And(left: Predicate, right: Predicate, start: Int, end: Int) extends BooleanPredicate
  final case class Or(left: Predicate, right: Predicate, start: Int, end: Int) extends BooleanPredicate
  final case class Not(@(JSExport @field)expr: Predicate, start: Int, end: Int) extends Predicate

  sealed trait EqualityValue extends Product with Serializable
  final case class StringValue(@(JSExport @field)valueStr: String) extends EqualityValue
  final case object AllValue extends EqualityValue

  sealed trait Comparison extends Product with Serializable
  final case class Eq(@(JSExport @field)value: EqualityValue) extends Comparison
  final case class NotEq(@(JSExport @field)value: String) extends Comparison
  final case class In(@(JSExport @field)ls: Seq[String]) extends Comparison
}
