package scalaExcel

import java.util.Locale

import scalaExcel.formula._
import org.junit.Assert._
import org.junit._
import scalaExcel.model.{DefaultValueFormat, Styles}

class MyTest {

  @Test def testDefaultFormat() {
    println("in MyTest")
    val style = Styles.DEFAULT.setFormat(DefaultValueFormat)
    assertEquals("123.46", style.format(VDouble(123.4567)))
  }

}
