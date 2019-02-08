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
    val sheet1 = sheet.setCell( (0,0), "100")
    val sheet2 = sheet1.setCell( (0,1), "50")
    val sheet3 = sheet2.setCell( (0,2), "=A1+A2")
    println(pprint.apply(sheet3.getCell(0, 2).AST))

    assert(sheet3.getValue( (0,2) ) == VDouble(150.0))

    //val style = Styles.DEFAULT.setFormat(DefaultValueFormat)
    //assertEquals("123.46", style.format(VDouble(123.4567)))
  }

}
