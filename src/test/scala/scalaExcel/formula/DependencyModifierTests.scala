package scalaExcel.formula

import java.{util => ju, lang => jl}

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import scalaExcel.model.CellPos
import scalaExcel.formula.DependencyModifier.{changeDependency, moveDependencies, changeDependencyRows}
import scalaExcel.formula.PPrinter.pprint

@RunWith(value = classOf[Parameterized])
class DependencyModifierParameterizedTests(orf: String, from : CellPos, to : CellPos, cutf : String, copyf : String) {
  val p = new Parser()

  val or = p parsing orf
  val cut = p parsing cutf
  val copy = p parsing copyf

  @Test def testChangeDependency() = assertEquals(cut, changeDependency(from, to)(or))

  @Test def testMoveDependencies() = assertEquals(copy, moveDependencies(from, to)(or))
}

object DependencyModifierParameterizedTests {
  @Parameters def parameters: ju.Collection[Array[jl.Object]] = {
    val data = new ju.ArrayList[Array[jl.Object]]()
    List(
      ("a1", (0,0), (500,123), "a1", "a1"),
      ("=A1", (0,0), (1,2), "=B3", "=B3"),
      ("=A1", (1,1), (2,3), "=A1", "=B3"),
      ("=SUM(A1:A5)", (0,0), (1,2), "=SUM(A1:A5)", "=SUM(B3:B7)"),
      ("=B1+C1", (2,0), (3,2), "=B1+D3", "=C3+D3"),
      ("=$B1+C$1", (2,0), (3,2), "=$B1+D$3", "=$B3+D$1"),
      ("=-D10", (9,4), (1,1), "=-D10", "=-#REF!"),
      ("=-D10", (3,9), (1,4), "=-B5", "=-B5"),
      ("=-D10", (2,8), (1,4), "=-D10", "=-C6")
      ).foreach({case (a,b,c,d,e) => data.add(Array(a,b,c,d,e))})
    data
  }
}

class DependencyModifierTests {

  val parser= new Parser()
  def p = parser parsing _

  def change(s: String) =
    pprint(changeDependencyRows(Map(
      0 -> 4,
      1 -> 5
    ))(p(s)))

  @Test def changeDependencyRowsTest() = {
    assertEquals("=A5", change("=A1"))
    assertEquals("=A6", change("=A2"))
    assertEquals("=A5 + A6", change("=A1 + A2"))
    assertEquals("=A4", change("=A4"))
  }

}
