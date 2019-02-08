package scalaExcel

import java.util.Locale

import scalaExcel.formula._
import org.junit.Assert._
import org.junit._
import scalaExcel.model.immutable.Sheet
import scalaExcel.model.{DefaultValueFormat, Styles}

class MyTest {

  @Test def testDefaultFormat() {

    val sheet = new Sheet()
    val a = sheet.setCell( (0,0), "100")
    val b = a.setCell( (0,1), "=A1")
    assert(b.getValue( (0,1) ) == VDouble(100.0))

    //val style = Styles.DEFAULT.setFormat(DefaultValueFormat)
    //assertEquals("123.46", style.format(VDouble(123.4567)))
  }

}
