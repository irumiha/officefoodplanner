package org.codecannery.lunchplanner

import utest._

object FrontendAppTest extends TestSuite {

  def tests = Tests {
    "Hello" - {
      assert("Hello world" == "Hello world")
    }
  }
}