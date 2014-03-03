package org.scalajs.spickling.test

import utest._

import org.scalajs.spickling.jsany._
import org.scalajs.spickling.Pickler._
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ literal => lit }
import org.scalajs.spickling.Pickler


case class Person(name: String, age: Int)

class PicklerTest extends TestSuite {

  def tests = TestSuite{
    "case classes" - {
      "Person" - {
        val p: Pickler[Person] = Pickler.materializePickler[Person]
        println(RichPicklee(Person("Ben", 40))(p).pickle(builder))
//        assert(RichPicklee(Person("Ben", 40))(p).pickle(builder) ==
//          lit(t = "org.scalajs.spickling.test.Person", v = lit(
//            name = lit(t = "java.lang.String", v = "Ben"),
//            age = lit(t = "java.lang.Integer", v = 40))))
      }
    }
  }
}
