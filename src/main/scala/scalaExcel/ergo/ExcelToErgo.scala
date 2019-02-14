package scalaExcel.ergo

import com.sun.org.apache.xpath.internal.operations.NotEquals
import scalaExcel.formula.Evaluator.Ctx
import scalaExcel.formula._
import scalaExcel.model.Sheet

object ExcelToErgo {

  val FUNCTION_SUM = "SUM"
  val FUNCTION_AVERAGE = "AVERAGE"
  val FUNCTION_POWER = "POWER"
  val FUNCTION_ROUND = "ROUND"
  val FUNCTION_ROW = "ROW"
  val FUNCTION_ROWS = "ROWS"
  val FUNCTION_COLUMN = "COLUMN"
  val FUNCTION_COLUMNS = "COLUMNS"
  val FUNCTION_COUNT = "COUNT"
  val FUNCTION_MATCH = "MATCH"
  val FUNCTION_VLOOKUP = "VLOOKUP"
  val FUNCTION_ADDRESS = "ADDRESS"
  val FUNCTION_IF = "IF"
  val FUNCTION_OR = "OR"
  val FUNCTION_AND = "AND"
  val FUNCTION_NOT = "NOT"
  val FUNCTION_UPPER = "UPPER"
  val FUNCTION_LOWER = "LOWER"
  val FUNCTION_LEN = "LEN"
  val FUNCTION_TRIM = "TRIM"
  val FUNCTION_TRUNC = "TRUNC"
  val FUNCTION_ISBLANK = "ISBLANK"
  val FUNCTION_ISERROR = "ISERROR"
  val FUNCTION_ISNA = "ISNA"
  val FUNCTION_ISLOGICAL = "ISLOGICAL"
  val FUNCTION_IISNUMBER = "ISNUMBER"
  val FUNCTION_ISTEXT = "ISTEXT"

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

  sealed class ErgoExpression

  case class ErgoInputName(name: String) extends ErgoExpression
  sealed class OperationType extends ErgoExpression

  object Sum extends OperationType
  object Neg extends OperationType
  object Mult extends OperationType
  object Divide extends OperationType
  object Equals extends OperationType
  object GreaterThan extends OperationType
  object LessThan extends OperationType
  object GreaterThanOrEquals extends OperationType
  object LessThanOrEquals extends OperationType
  object NoEquals extends OperationType
  object Concatenate extends OperationType
  object Exponent extends OperationType
  object Percentage extends OperationType

  case class Expression1(operationType: OperationType, left: ErgoExpression) extends ErgoExpression
  case class Expression2(operationType: OperationType, left: ErgoExpression, right: ErgoExpression) extends ErgoExpression
  case class ErgoNumber(number: Double = 0.0d) extends ErgoExpression
  case class ErgoString(valueOf: String = "") extends ErgoExpression
  case class FunctionCall(functionName: String = "", parameters: Seq[ErgoExpression]) extends ErgoExpression
  case class Nothing() extends ErgoExpression

  case class ErgoLine(line: String)
  case class Ergo(lines: Seq[ErgoLine])

  // A binding to a cell in a spreadsheet has a variable name
  case class Binding(variableName: String, cellCol: Int, cellRow: Int, ast: Expr)
  case class Bindings(bindings: Seq[Binding])

  case class Spreadsheet(bindingsOut: Bindings)

  // eg. let name : String = "Hello";
  // let x : Double = 3.1416;
  case class LetBinding(name: String, dataType: DataType, expression: ErgoExpression)

  sealed class DataType
  object DataTypeDouble extends DataType
  object DataTypeInt extends DataType
  object DataTypeString extends DataType

  // A reference to a data model field
  case class DataModelFieldReference(modelFieldName: String)
  case class DataModelFieldReferenceOutput(fieldNameInTheOutput: String, ioSheet: IOSheet, cellOutput: CellOutput)
  case class DataModelFieldReferenceInput(modelFieldName: String/*, spreadsheetName: String, spreadsheetOutputName: String, spreadsheetParameterName: String*/)
  // Hard to explain
  case class DataModelFieldReferenceBinding(modelFieldName: String, spreadsheetName: String, spreadsheetOutputName: String, spreadsheetParameterName: String)
  // Reference in a spreadsheet
  case class CellReference(typeOf: DataType, cellCol: Int, cellRow: Int)
  case class CellInput(name: String, cellReference: CellReference, modelFieldReference: DataModelFieldReference)
  case class CellOutput(outputFunctionName: String, cellReference: CellReference)

  case class IOSheet(name: String, input: Seq[CellInput], output: Seq[CellOutput], sheet: Sheet)




  // Convert from Calculations to ErgoCode
  case class Calculations(sheets: Seq[IOSheet],
                          importPackages: Seq[String],
                          namespace: String,
                          contractName: String,
                          clauseFunctionName: String,
                          requestClassName: String,
                          responseClassName: String,
                          inputBindings: Seq[DataModelFieldReferenceInput],
                          outputBindings: Seq[DataModelFieldReferenceOutput]
    )
  case class ErgoCode(code: Seq[ErgoLine])
  object ErgoCode {
    def changeToString(code: Seq[ErgoLine]): String = {
      (for (c <- code) yield c.line).mkString("\n")
    }

    def fromString(str: String) = {
      ErgoCode(Seq(ErgoLine(str)))
    }
  }

  case class ErgoCodeGen(calculations: Calculations, ergoCode: Seq[ErgoCode])




  def percentToDoubleString(code: String): String = {
    // Assume code is a number
    val d = (code.toDouble * 0.01d)
    "%1.4f".format(d)
  }

  private def ergoTreeToCode(tree: ErgoExpression, inputs: Seq[CellInput]): ErgoCode = {
    tree match {
      case num: ErgoNumber => ErgoCode.fromString(s"${num.number}")
      case Expression2(opType: OperationType, left, right) => {
        val op = opType match {
          case Sum => "+"
          case Neg => "-"
          case Mult => "*"
          case Divide => "/"
          case Exponent => "^"
          case Equals => "=="
          case NoEquals => "!="
          case z: Object => {
            println("unhandled case: " + z)
            ""
          }
        }
        val a = for (r <- Seq(left, right)) yield ergoTreeToCode(r, inputs)
        ErgoCode.fromString(s"(${ErgoCode.changeToString(a(0).code)}) $op (${ErgoCode.changeToString(a(1).code)})")
      }
      case Expression1(opType: OperationType, left) => {
        val code = ergoTreeToCode(left, inputs)
        opType match {
          case Percentage => ErgoCode.fromString(percentToDoubleString(ErgoCode.changeToString(code.code)))
          case z: Object => {
            println("Unhandled case " + z)
            ErgoCode.fromString("")
          }
        }
      }
      case ErgoInputName(name: String) => {
        ErgoCode.fromString(name)
      }
      case FunctionCall(functionName, parameters: Seq[ErgoExpression]) => {
        val parametersCode = for (p <- parameters) yield {
          val code = ErgoCode.changeToString(ergoTreeToCode(p, inputs).code)
          s"($code)"
        }
        functionName match {
          case FUNCTION_SUM => ErgoCode.fromString(parametersCode.mkString(" + "))
          case FUNCTION_AVERAGE => {
            val commaSep = parametersCode.mkString(", ")
            ErgoCode.fromString(s"average([$commaSep])")
          }
          case FUNCTION_POWER => {
            val sep = parametersCode.mkString(" ^ ")
            ErgoCode.fromString(sep)
          }
          /*@todo Unknown so far...
          case FUNCTION_ROUND => {
            val sep = parametersCode.mkString(" ^ ")
            ErgoCode(sep)
          }*/
          case FUNCTION_ROUND => {
            ErgoCode.fromString("The case is not handled yet")
          }
          case FUNCTION_ROW => {
            ErgoCode.fromString(parametersCode.head)
          }
          case FUNCTION_ROWS => {
            ErgoCode.fromString(parametersCode.head)
          }
          case FUNCTION_COLUMN => {
            ErgoCode.fromString(parametersCode.head)
          }
          case FUNCTION_COLUMNS => {
            ErgoCode.fromString(parametersCode.head)
          }
          case _ => {
            ErgoCode.fromString("This case is not handled yet")
          }
        }
      }
      case other: Object => {
        println("unhandled case 55: " + other)
        ErgoCode.fromString("")
      }
    }
  }

  case class InputNameElseCellRef(name: Option[String], cell: Option[scalaExcel.model.Cell])

  private def getCellWrap(sheet: Sheet, inputs: Seq[CellInput], col: Int, row: Int): InputNameElseCellRef = {
    val f = inputs.find(c => (c.cellReference.cellCol == col) && (c.cellReference.cellRow == row))
    if (f.isDefined) {
      InputNameElseCellRef(Some(f.get.name), None)
    }
    else
      InputNameElseCellRef(None, Some(sheet.getCell((col, row))))
  }

  private def cellWrapToErgoTree(sheet: Sheet, cellWrap: InputNameElseCellRef, inputs: Seq[CellInput]): ErgoExpression = {
    if (cellWrap.cell.isDefined)
      cellWrapToErgoTree(sheet, cellWrap.cell.get.AST, inputs)
    else
      ErgoInputName(cellWrap.name.get)
  }

  def evalRowRange(sheet: Sheet, e1Rows: Range, inputs: Seq[CellInput]): ErgoExpression = {
    val start = e1Rows.start
    val end = e1Rows.end
    println("Start: " + start.r.referent)
    println("End: " + end.r.referent)
    println("Diff: " + Math.abs(start.r.referent - end.r.referent) + 1)
    ErgoNumber(Math.abs(start.r.referent - end.r.referent) + 1)
  }

  def evalColRange(sheet: Sheet, e1Rows: Range, inputs: Seq[CellInput]): ErgoExpression = {
    val start = e1Rows.start
    val end = e1Rows.end
    ErgoNumber(Math.abs(start.c.referent - end.c.referent) + 1)
  }


  def evalMatchFunction(function: String, sheet: Sheet, e1Rows: Range, inputs: Seq[CellInput]): ErgoExpression = {
    //val start = e1Rows.start
    //val end = e1Rows.end

    //Evaluator.evalCall(ctx: Ctx, function, args: List[Expr])

    ErgoNumber(0)
  }

  def cellOrOther(e1: Expr, sheet: Sheet, inputs: Seq[CellInput], functionCall: String = ""): ErgoExpression = {
    e1 match {
      case e1Cell: Cell => cellWrapToErgoTree(sheet, getCellWrap(sheet, inputs, e1Cell.c.referent, e1Cell.r.referent), inputs)
      case e1Const: Const => fromValue(e1Const.v)
      case e1Value: Value => fromValue(e1Value)
      // BinOp(Plus(),Const(VDouble(2.0)),BinOp(Div(),Const(VDouble(2.0)),Const(VDouble(0.5))))
      case e1BinOp: BinOp => cellWrapToErgoTree(sheet, e1BinOp, inputs)
      //Group(BinOp(Div(),Const(VDouble(1.2)),Const(VDouble(2.0))))
      case e1Group: Group => cellWrapToErgoTree(sheet, e1Group.e, inputs)
      case r1Range: Range => {
        functionCall match {
          case FUNCTION_ROWS => evalRowRange(sheet, r1Range, inputs)
          case FUNCTION_COLUMNS => evalColRange(sheet, r1Range, inputs)
          case FUNCTION_MATCH => evalMatchFunction(FUNCTION_MATCH, sheet, r1Range, inputs)
          case other: Object => {
            println("unhandled case lala")
            println(other)
            null
          }
        }
      }
      case e1What: Object => println("Unhandled case z: " + e1What); null
    }
  }

  private def cellWrapToErgoTree(sheet: Sheet, ast: Expr, inputs: Seq[CellInput]): ErgoExpression = {

    def commonality(op: Op, opType: OperationType, e1: Expr, e2: Expr) = {
      Expression2(opType, cellOrOther(e1, sheet, inputs), cellOrOther(e2, sheet, inputs))
    }

    def commonality2(op: Op, opType: OperationType, e1: Expr) = {
      Expression1(opType, cellOrOther(e1, sheet, inputs))
    }

    //println(ast)
    ast match {
      // 1 + 1
      case BinOp(Plus(), e1: Expr, e2: Expr) => commonality(Plus(), Sum, e1, e2)
      // 1 - 1
      case BinOp(Minus(), e1: Expr, e2: Expr) => commonality(Minus(), Neg, e1, e2)
      // 1 * 1
      case BinOp(Mul(), e1: Expr, e2: Expr) => commonality(Mul(), Mult, e1, e2)
      // 1 / 1
      case BinOp(Div(), e1: Expr, e2: Expr) => commonality(Div(), Divide, e1, e2)
      // 1 == 1
      case BinOp(Eq(), e1: Expr, e2: Expr) => commonality(Eq(), Equals, e1, e2)
      // 1 > 1
      case BinOp(GT(), e1: Expr, e2: Expr) => commonality(GT(), GreaterThan, e1, e2)
      // 1 < 1
      case BinOp(LT(), e1: Expr, e2: Expr) => commonality(LT(), LessThan, e1, e2)
      // 1 >= 1
      case BinOp(GTE(), e1: Expr, e2: Expr) => commonality(GTE(), GreaterThanOrEquals, e1, e2)
      // 1 <= 1
      case BinOp(LTE(), e1: Expr, e2: Expr) => commonality(LTE(), LessThanOrEquals, e1, e2)
      // 1 != 1
      case BinOp(NEq(), e1: Expr, e2: Expr) => commonality(NEq(), NoEquals, e1, e2)
      // Concatenate ?
      case BinOp(Concat(), e1: Expr, e2: Expr) => commonality(Concat(), Concatenate, e1, e2)
      // 5 ^ 2
      case BinOp(Expon(), e1: Expr, e2: Expr) => commonality(Expon(), Exponent, e1, e2)
      // 10%
      case UnOp(Percent(), e1: Expr) => commonality2(Percent(), Percentage, e1)

      case Cell(ColRef(col: Int,false), RowRef(row: Int,false)) => {
        cellWrapToErgoTree(sheet, getCellWrap(sheet, inputs, col, row), inputs)
      }

      case Const(v: VDouble) => {
        ErgoNumber(v.d)
      }
      case Const(v: Any) => {
        if (v==VEmpty) {
          println(v)
          println("In this case return empty string? or error?")
          ErgoNumber(0.0d)
        } else {
          println("What is this case")
          println(v)
          ErgoNumber(0.0d)
        }
      }
      case UnOp(Percent(), e1: Expr) => commonality2(Percent(), Percentage, e1)

      case Call(FUNCTION_ROWS, cells: List[Expr]) => {
        cells.head match {
          case e1Rows: Range => evalRowRange(sheet, e1Rows, inputs)
          case other: Object => {
            println("unhandled case 95: " + other)
            null
          }
        }
      }
      case Call(f, cells: List[Expr]) => {
        val parameters = for (c <- cells) yield cellOrOther(c, sheet, inputs, f)
        FunctionCall(f, parameters)
      }

      case other: Any => {
        println("Unhandled case 2: " + other)
        Nothing()
      }
    }
  }




  // Returns type of a string as excel code interprets
  def getType(s: String) = {
    def removeParentheses(e : Expr) : Expr = e match {
      case Group(e1) => removeParentheses(e1)
      case UnOp(op, e1) => UnOp(op, removeParentheses(e1))
      case BinOp(op, e1, e2) => BinOp(op, removeParentheses(e1), removeParentheses(e2))
      case Call(f, args) => Call(f, args.map(removeParentheses))
      case other: Any => {
        //println("Unhandled case 3: " + other)
        e
      }
    }
    val parsed = removeParentheses(Parser parsing s)
    parsed
  }

  def fromValue(value: Value): ErgoExpression = {
    value match {
      case d: VDouble => ErgoNumber(d.d)
      case s: VString => ErgoString(s.s)
      //case other: Value => ErgoNumber(0.0f)
      case other: Any => {
        println("Unhandled case 4: " + other)
        ErgoNumber(0.0f)
      }
    }
  }

  def toErgoLine(sheet: Sheet, ast: Expr, inputs: Seq[CellInput]): Seq[ErgoLine] = {

    val tree = cellWrapToErgoTree(sheet, ast, inputs)
    ergoTreeToCode(tree, inputs).code
  }

  def toErgoCode(functionName: String, sheet: IOSheet, bindings: Bindings, inputs: Seq[CellInput]): Ergo = {

    val inputParameters = (for (i <- inputs) yield s"${i.name}: Double").mkString(", ")

    val lines: Seq[ErgoLine] = (for (b <- bindings.bindings) yield {
      val v = ergoTreeToCode(cellWrapToErgoTree(sheet.sheet, b.ast, inputs), inputs)
      Seq(ErgoLine(s"  let ${b.variableName} : Double = ${ErgoCode.changeToString(v.code)};"),
        ErgoLine(s"  let result : Double = ${b.variableName};"),
      ErgoLine(s"  return result;"))
    }).flatten

    val lines2: Seq[ErgoLine] = ErgoLine(s"define function ${functionName}($inputParameters) : Double {") +: lines
    val lines3: Seq[ErgoLine] = lines2 :+ ErgoLine("}")

    Ergo(lines3)
  }

}
