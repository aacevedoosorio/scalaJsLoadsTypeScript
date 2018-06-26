package parser

import parser.AST._
import fastparse.all._
import fastparse.core.Parsed.{Failure, Success}

import scala.annotation.tailrec
import scala.scalajs.js
import scala.scalajs.js.annotation._

//@ScalaJSDefined
//@JSExport("SQLParser")

//@JSExportTopLevel("SQLParser")

//@JSImport("/Users/alejandro/Projects/scalaJsTest/src/main/resources/test.ts", "Foo")
@js.native
@JSImport("./test", "Foo")
object JSFoo extends js.Object {
  def add(a: Int, b: Int): Int = js.native
}


@JSExportTopLevel(name="stackstate.Parser")
object Parser {

  // literals
  private val starLit = "*"
  private val notLit = "not"
  private val andLit = "and"
  private val orLit = "or"
  private val eqLit = "="
  private val notEqLit = "!="
  private val inLit = "in"

  // literal parsers
  private val doubleQuote: P[Unit] = P(CharIn("""""""))
  private val singleQuote: P[Unit] = P(CharIn("""'"""))
  private val starOp: P[String] = P(starLit.!)
  private val char: P[Unit] = P(CharIn('a' to 'z', 'A' to 'Z', '0' to '9', "_", ":", """/""", """\""", ".", "-"))
  private val keys: P[String] = P(CharIn('a' to 'z', 'A' to 'Z').! ~ char.rep.!).map { case (initChar, rest) => initChar + rest }
  private val values: P[String] = P(char.rep(1).!)

  private val spaces: P[Unit] = P(CharsWhile(" \t\r\n" contains _))
  private val quotedStr: P[String] = P((doubleQuote ~ values ~/ doubleQuote) | (singleQuote ~ values ~/ singleQuote))
  private val quotedStarOp: P[String] = P((doubleQuote ~ starOp ~/ doubleQuote) | (singleQuote ~ starOp ~/ singleQuote))
  private val valueList: P[Seq[String]] = P("(" ~/ spaces.? ~ quotedStr.rep(1, P(spaces.? ~ "," ~ spaces.?)) ~ spaces.? ~ ")")

  // key-value parsers
  private val listComparison: P[(String, Int, Int, Int) => KeyValue] =
    P(spaces.rep(1) ~ inLit ~ spaces.rep ~ Index ~ valueList).map {
      case (valStart, vals) => (key, start, keyEnd, end) => KeyValue(Key(key, start, keyEnd), Value(In(vals), valStart, end), start, end)
    }
  private val neqComparison: P[(String, Int, Int, Int) => KeyValue] =
    P(spaces.? ~ notEqLit ~/ spaces.? ~ Index ~ quotedStr).map {
      case (valStart, value) => (key, start, keyEnd, end) => KeyValue(Key(key, start, keyEnd), Value(NotEq(value), valStart, end), start, end)
    }
  private val eqComparison: P[(String, Int, Int, Int) => KeyValue] =
    P(spaces.? ~ eqLit ~/ spaces.? ~ Index ~ (quotedStarOp | quotedStr)).map {
      case (valStart, value) =>
        (key, start, keyEnd, end) => KeyValue(Key(key, start, keyEnd), Value(Eq(toEqualityValue(value)), valStart, end), start, end)
    }
  private val keyValue: P[KeyValue] =
    P(Index ~ keys ~ Index ~ (listComparison | eqComparison | neqComparison) ~ Index).map {
      case (start, key, opStart, comparator, end) =>
        comparator(key, start, opStart, end)
    }

  // boolean operator parsers
  private val not: P[Predicate] = P(Index ~ StringInIgnoreCase(notLit) ~/ spaces.rep(1) ~ factor ~ Index) map {
    case (start, f, end) =>
      Not(f, start, end)
  }
  private val parens: P[Predicate] = P("(" ~/ spaces.? ~ and ~ spaces.? ~ ")")
  private val factor: P[Predicate] = P(keyValue | parens | not)

  private val or: P[Predicate] = P(factor ~ (spaces.rep(1) ~ StringInIgnoreCase(orLit).! ~/ spaces.rep(1) ~ factor).rep) map eval

  private val and: P[Predicate] = P(or ~ (spaces.rep(1) ~ StringInIgnoreCase(andLit).! ~/ spaces.rep(1) ~ or).rep) map eval

  // predicate parser
  private val query: P[Predicate] =
    P(spaces.rep ~ and ~ spaces.rep ~ End)

  /**
    * This function receives the initial expression followed by a sequence of tuples (boolean operators [OR, AND], another expression).
    * It recursively folds the sequence of tuples and transforms them to the proper AST instances. Some instances were parsed
    * at the individual combinator level, however, to avoid infinite recursion, the 'ANDs' and 'ORs' are parsed in this function.
    */
  private def eval(expressions: (Predicate, Seq[(String, Predicate)])): Predicate = {
    val (initialExpr, rest) = expressions

    rest
      .foldLeft(initialExpr) {
        case (accumulatedExpr, (expType, innerExp)) if expType.equalsIgnoreCase(andLit) =>
          And(accumulatedExpr, innerExp, accumulatedExpr.start, innerExp.end)
        case (accumulatedExpr, (expType, innerExp)) if expType.equalsIgnoreCase(orLit) =>
          Or(accumulatedExpr, innerExp, accumulatedExpr.start, innerExp.end)
      }
  }

  private def toEqualityValue(str: String): EqualityValue = {
    if (str == starLit) AllValue else StringValue(str)
  }

  @JSExport
  def find(pos: Int, predicate: Clause): Option[Clause] = {
    // Intervals are exclusive in the right side
    val between = (pos: Int, start: Int, end: Int) => start <= pos && end > pos

    predicate match {
      case bp: BooleanPredicate =>
        val left = bp.left
        val right = bp.right
        if (between(pos, left.start, left.end)) {
          find(pos, left)
        } else if (between(pos, right.start, right.end)) {
          find(pos, right)
        } else if (between(pos, bp.start, bp.end)) {
          Some(bp)
        } else {
          None
        }
      case key @ Key(_, start, end) =>
        if (between(pos, start, end)) Some(key) else None
      case value @ Value(_, start, end) =>
        if (between(pos, start, end)) Some(value) else None
      case kv @ KeyValue(key, value, start, end) =>
        if (between(pos, key.start, key.end)) {
          find(pos, key)
        } else if (between(pos, value.start, value.end)) {
          find(pos, value)
        } else if (between(pos, start, end)) {
          Some(kv)
        } else {
          None
        }
      case not @ Not(p, start, end) =>
        if (between(pos, p.start, p.end)) {
          find(pos, p)
        } else if (between(pos, start, end)) {
          Some(not)
        } else {
          None
        }
    }
  }

  //@JSExportTopLevel("Parser.parse")
  @JSExport
  def parse(input: String): Parsed[Predicate] = {

    val a = query.parse(input)

    a match {
      case Parsed.Success(e,i) => println(s"Success")
      case Parsed.Failure(x: fastparse.parsers.Combinators.Either[_, Char, String], in, extra) => println(s"ERRRRROR $x")
      case Parsed.Failure(y, in, extra) => println(s"PATERN MATCH DEFAULT $y")
    }

    println(JSFoo.add(1, 5))

    a
  }
}

