package parser

import org.scalatest.WordSpec

final class MyModuleTest extends WordSpec {
  "MyModule" should {
    "has a someUuid field" in {
      assert(MyModule.someUuid != null)
    }

  }
}
