package scalaExcel

import java.util.Locale

import scalaExcel.formula._
import org.junit.Assert._
import org.junit._
import scalaExcel.ergo.ExcelToErgo
import scalaExcel.ergo.ExcelToErgo._
import scalaExcel.model.immutable.Sheet
import scalaExcel.model.{DefaultValueFormat, Styles}

import scala.collection.mutable

class MyTest {

  def testSpreadsheets(): Seq[IOSheet] = {

    val sheets = mutable.Buffer[Sheet]()
    val inputs = mutable.Buffer[Seq[CellInput]]()
    val outputs = mutable.Buffer[Seq[CellReference]]()

    sheets += new Sheet()
      .setCell( (0,0), "100")
      .setCell( (0,1), "50")
      .setCell( (0,2), "=A1+A2")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 0, 1), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 0, 2))

    sheets += new Sheet()
      .setCell((0, 0), "1")
      .setCell((1, 0), "=2+2/0.5+(1.2/2.0)")
      .setCell((0, 1), "=A1+1")
      .setCell((1, 1), "=$A$1+1")
      .setCell((3, 0), "=B1/0.5")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 1, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 3, 0))

    sheets += new Sheet()
      .setCell((0, 0), "1")
      .setCell((1, 0), "2")
      .setCell((0, 1), "=A1*1")
      .copyCell((0, 1), (1, 1))

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 1, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 1, 1))

    sheets += new Sheet()
      .setCell((0, 0), "0")
      .setCell((0, 1), "2")
      .setCell((0, 2), "=A1+A2") // 1
      .setCell((0, 3), "=A2*A3") // 2
      .setCell((0, 4), "=A3/A4") // 3
      .setCell((0, 5), "=A4+A5") // 5

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 0, 1), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 0, 5))

    sheets += new Sheet()
      .setCell((0, 0), "100")
      .setCell((0, 1), "2")
      .setCell((0, 2), "=A1^A2")
      .setCell((0, 3), "=A3")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "100")
      .setCell((0, 1), "101")
      .setCell((0, 2), "=A1 <> A2")
      .setCell((0, 3), "=A3")

    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 0, 3))


    sheets += new Sheet()
      .setCell((0, 0), "=95.2%")
      .setCell((0, 1), "=1000.0")
      .setCell((0, 2), "=A1*A2")

    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellReference(DataTypeDouble, 0, 2))


    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=SUM (A1,A2,A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=AVERAGE(A1,A2,A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "=3")
      .setCell((0, 1), "=2")
      .setCell((0, 3), "=POWER(A1,A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "=1.058329234")
      .setCell((0, 1), "=2")
      .setCell((0, 3), "=ROUND(A1,A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=ROW(A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))


    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=ROWS(A1:A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))



    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=COLUMN(C3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=COLUMNS(C3:D5)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))

    /* @todo count
    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=COUNT(C3:D5)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))*/

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=MATCH(C3:D5)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellReference(DataTypeDouble, 0, 3))



    /*
      case "SUM"     => reduce(ctx, applyToDoubles(_ + _), VDouble(0), desugarArgs(args))
      case "AVERAGE" => evalCallAverage(ctx, desugarArgs(args))
      case "POWER"   => evalCallPower(ctx, args)
      case "ROUND"   => evalCallRound(ctx, args)
      case "ROW"     => evalCallRow(args)
      case "ROWS"    => evalCallRows(args)
      case "COLUMN"  => evalCallColumn(args)
      case "COLUMNS" => evalCallColumns(args)
      case "COUNT"   => evalCallCount(ctx, desugarArgs(args))
      case "MATCH"   => evalCallMatch(ctx, args)
      case "VLOOKUP" => evalCallVLookUp(ctx, args)
      case "ADDRESS" => evalCallAddress(ctx, args)
      case "IF"      => evalCallIf(ctx, args)
      case "OR"      => evalCallOr(ctx, desugarArgs(args))
      case "AND"     => evalCallAnd(ctx, desugarArgs(args))
      case "NOT"     => evalCallNot(ctx, args)
      case "UPPER"   => evalCallUpper(ctx, args)
      case "LOWER"   => evalCallLower(ctx, args)
      case "LEN"     => evalCallLen(ctx, args)
      case "TRIM"    => evalCallTrim(ctx, args)
      case "TRUNC"   => evalCallTrunc(ctx, args)
      case "ISBLANK"   => evalCallIsBlank(ctx, desugarArgs(args))
      case "ISERROR"   => evalCallIsError(ctx, desugarArgs(args))
      case "ISNA"      => evalCallIsNA(ctx, desugarArgs(args))
      case "ISLOGICAL" => evalCallIsLogical(ctx, desugarArgs(args))
      case "ISNUMBER"  => evalCallIsNumber(ctx, desugarArgs(args))
      case "ISTEXT"    => evalCallIsText(ctx, desugarArgs(args))
     */
    // ##############################################################################################################

    val inputs2 = inputs.toSeq
    val outputs2 = outputs.toSeq

    val z = sheets.toSeq
    for (s <- z.zip(inputs2).zip(outputs2).zipWithIndex) yield { IOSheet(s"sheet${s._2}", s._1._1._2, s._1._2, s._1._1._1) }
  }

  def nameFromCell(sheetName: String, out: CellReference): String = {
    s"${sheetName}CellC${out.cellCol}R${out.cellRow}"
  }

  @Test def testDefaultFormat() {

    val t = ExcelToErgo.getType("1")
    println(t)

    val sheets = testSpreadsheets()
    for (sheet <- sheets) {
      println("")
      println("Sheet " + sheet.name + " ---------------------------------------------------------------------------------------")
      for (out <- sheet.output) {
        val ast = sheet.sheet.getCell(out.cellCol, out.cellRow).AST
        val all = Seq(Binding(nameFromCell(sheet.name, out), out.cellCol, out.cellRow, ast))
        val ergo = ExcelToErgo.toErgoCode(sheet.sheet, Bindings(all), Seq.empty)
        for (e <- ergo.lines) {
          println(e.line)
        }
        println(sheet.sheet.getValue((out.cellCol, out.cellRow)))
      }
    }
    println("")
  }

}
