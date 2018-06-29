package parser

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("./my-module", JSImport.Namespace)
@js.native
object MyModule extends js.Object {
  val someUuid: String = js.native
}

@js.native
@JSImport("./my-module", "Foo")
object JSFoo extends js.Object {
  def add(a: Int, b: Int): Int = js.native
}