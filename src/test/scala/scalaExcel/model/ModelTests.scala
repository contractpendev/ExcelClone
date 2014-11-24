package scalaExcel.model

import scalaExcel.formula.{VErr, CircularRef, VDouble, Value}
import scalaExcel.model.OperationHelpers._

import org.junit.Assert._
import org.junit._

class ModelTests {

  @Test def initializeModel() = {
    val model = new Model()
    assertEquals(Map(), model.sheet.take(1).toBlocking.last.values)
  }

  @Test def changeFormula() = {
    val model = new Model()
    val cell = model.sheet
      .filterCellValueAt(1, 1)
    model.changeFormula(1, 1, "=1+1")
    assertEquals(VDouble(2), cell.take(1).toBlocking.last)
  }

  @Test def formulaWithReferences() = {
    val model = new Model()
    var y: Value = null
    model.sheet
      .filterCellValueAt(1, 3)
      .subscribe(x => y = x)

    model.changeFormula(1, 1, "=1+1")
    model.changeFormula(1, 2, "=A1 + 1")
    model.changeFormula(1, 3, "=A2 * 2")

    assertEquals(VDouble(6), y)
  }

  @Test def formulaUpdateDependents() = {
    val model = new Model()
    var y: Value = null
    model.sheet
      .filterCellValueAt(1, 2)
      .subscribe(x => y = x)

    model.changeFormula(1, 1, "=1+1")
    model.changeFormula(1, 2, "=A1 + 1")
    model.changeFormula(1, 1, "=9")

    assertEquals(VDouble(10), y)
  }

  @Test def circularDependency1() = {
    val model = new Model()
    var y: Value = null
    model.sheet
      .filterCellValueAt(3, 1)
      .subscribe(x => y = x)

    model.changeFormula(1, 1, "=C1")
    model.changeFormula(2, 1, "=5")
    model.changeFormula(3, 1, "=A1+B1")

    assertEquals(VErr(CircularRef()), y)
  }

  @Test def circularDependency2() = {
    val model = new Model()
    var y: Value = null
    model.sheet
      .filterCellValueAt(3, 1)
      .subscribe(x => y = x)

    model.changeFormula(1, 1, "=B1")
    model.changeFormula(2, 1, "=C1+1")
    model.changeFormula(3, 1, "=A1+1")

    assertEquals(VErr(CircularRef()), y)
  }

  @Test def circularDependency3() = {
    val model = new Model()
    var y: Value = null
    model.sheet
      .filterCellValueAt(1, 1)
      .subscribe(x => y = x)

    model.changeFormula(1, 1, "=5")
    model.changeFormula(2, 1, "=A1+5")
    model.changeFormula(3, 1, "=B1")
    model.changeFormula(1, 1, "=B1")

    assertEquals(VErr(CircularRef()), y)
  }
}
