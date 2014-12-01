package scalaExcel.model

import org.junit.Assert._
import org.junit._

import scalaExcel.formula._
import scalaExcel.formula.Values.{toVal => tv}
import scalaExcel.model.Sorter.SheetSorter

class SorterTests {

  @Test def testDenseSingleColumnSort() = {
    val model = new Model()
    var sheet: Sheet = null

    model.sheet.subscribe(s => sheet = s)

    model.changeFormula((0, 0), "=1")
    model.changeFormula((0, 1), "=4")
    model.changeFormula((0, 2), "=2")
    model.changeFormula((0, 3), "=3")
    model.sheetMutations.onCompleted

    val newSheet = sheet.sort(0)
    assertEquals(Some(VDouble(1)), newSheet.valueAt((0, 0)))
    assertEquals(Some(VDouble(2)), newSheet.valueAt((0, 1)))
    assertEquals(Some(VDouble(3)), newSheet.valueAt((0, 2)))
    assertEquals(Some(VDouble(4)), newSheet.valueAt((0, 3)))
  }

  @Test def testSparseColumnSort() = {
    val model = new Model()
    var sheet: Sheet = null

    model.sheet.subscribe(s => sheet = s)

    model.changeFormula((0, 0), "=1")
    model.changeFormula((0, 9), "=4")
    model.changeFormula((0, 4), "=2")
    model.changeFormula((0, 2), "=3")
    model.sheetMutations.onCompleted

    val newSheet = sheet.sort(0)
    assertEquals(Some(VDouble(1)), newSheet.valueAt((0, 0)))
    assertEquals(Some(VDouble(2)), newSheet.valueAt((0, 1)))
    assertEquals(Some(VDouble(3)), newSheet.valueAt((0, 2)))
    assertEquals(Some(VDouble(4)), newSheet.valueAt((0, 3)))
    assertEquals(None, newSheet.valueAt((0, 4)))
  }

  @Test def testColumnSortDesc() = {
    val model = new Model()
    var sheet: Sheet = null
    model.sheet.last.subscribe(s => sheet = s)

    model.changeFormula((0, 0), "=1")
    model.changeFormula((0, 2), "=4")
    model.changeFormula((0, 3), "=2")
    model.changeFormula((0, 4), "=3")
    model.sheetMutations.onCompleted

    val newSheet = sheet.sort(0, ascending = false)
    assertEquals(Some(VDouble(4)), newSheet.valueAt((0, 0)))
    assertEquals(Some(VDouble(3)), newSheet.valueAt((0, 1)))
    assertEquals(Some(VDouble(2)), newSheet.valueAt((0, 2)))
    assertEquals(Some(VDouble(1)), newSheet.valueAt((0, 3)))
    assertEquals(None, newSheet.valueAt((0, 4)))
  }

  @Test def testUpdateAllColumns() = {
    val model = new Model()
    var sheet: Sheet = null
    model.sheet.last.subscribe(s => sheet = s)

    model.changeFormula((0, 0), "=1")
    model.changeFormula((0, 2), "=4")
    model.changeFormula((0, 4), "=2")
    model.changeFormula((0, 1), "=3")

    model.changeFormula((1, 0), "=11")
    model.changeFormula((1, 2), "=12")
    model.changeFormula((1, 3), "=13")
    model.changeFormula((1, 1), "=14")

    model.sheetMutations.onCompleted

    val newSheet = sheet.sort(0)

    assertEquals(Some(VDouble(1)), newSheet.valueAt((0, 0)))
    assertEquals(Some(VDouble(2)), newSheet.valueAt((0, 1)))
    assertEquals(Some(VDouble(3)), newSheet.valueAt((0, 2)))
    assertEquals(Some(VDouble(4)), newSheet.valueAt((0, 3)))

    assertEquals(Some(VDouble(11)), newSheet.valueAt((1, 0)))
    assertEquals(None,              newSheet.valueAt((1, 1)))
    assertEquals(Some(VDouble(14)), newSheet.valueAt((1, 2)))
    assertEquals(Some(VDouble(12)), newSheet.valueAt((1, 3)))
    assertEquals(Some(VDouble(13)), newSheet.valueAt((1, 4)))
  }

  @Test def testUpdateDependents() = {
    val model = new Model()
    var sheet: Sheet = null
    model.sheet.last.subscribe(s => sheet = s)
    model.changeFormula((0, 0), "=3")
    model.changeFormula((0, 1), "=A1-1")
    model.changeFormula((0, 2), "=6+A1")
    model.changeFormula((0, 3), "=8")
    model.changeFormula((0, 4), "=A20")

    model.sheetMutations.onCompleted

    val newSheet = sheet.sort(0)

    assertEquals(Some(VDouble(0)), newSheet.valueAt((0, 0)))
    assertEquals(Some(VDouble(2)), newSheet.valueAt((0, 1)))
    assertEquals(Some(VDouble(3)), newSheet.valueAt((0, 2)))
    assertEquals(Some(VDouble(8)), newSheet.valueAt((0, 3)))
    assertEquals(Some(VDouble(9)), newSheet.valueAt((0, 4)))
    assertEquals(List((0, 1), (0, 4)), newSheet.dependents.get((0, 2)).get)
    assertEquals(List((0, 0)), newSheet.dependents.get((0, 19)).get)

    assertEquals("=A3 - 1", newSheet.getCell((0, 1)).f)
    assertEquals("=6 + A3", newSheet.getCell((0, 4)).f)
  }

}