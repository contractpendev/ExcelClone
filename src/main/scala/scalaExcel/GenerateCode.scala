package scalaExcel

import scala.collection.mutable
import java.util.Locale

import scalaExcel.ergo.ExcelToErgo
import scalaExcel.formula._
import scalaExcel.model.immutable.Sheet
import scalaExcel.model.{DefaultValueFormat, Styles}
import scalaExcel.ergo.ExcelToErgo._

import scala.collection.mutable

object GenerateCode {

  def nameFromCell(sheetName: String, out: CellReference): String = {
    s"${sheetName}CellC${out.cellCol}R${out.cellRow}"
  }

  def generateCode(codeGen: ErgoCodeGen): String = {
    def functionNameFrom(sheetName: String, outputName: String) = {
      "spreadsheetFunction_" + sheetName + "_" + outputName
    }

    val ergoCode = for (sheet <- codeGen.calculations.sheets) yield {
      val lines = mutable.Buffer[ErgoLine]()
      //println("")
      //println("Sheet " + sheet.name + " ---------------------------------------------------------------------------------------")
      // For each output we define a function
      for (out <- sheet.output) {
        val ast = sheet.sheet.getCell(out.cellReference.cellCol, out.cellReference.cellRow).AST
        val outputBindings = Seq(Binding(nameFromCell(sheet.name, out.cellReference), out.cellReference.cellCol, out.cellReference.cellRow, ast))
        val functionName = functionNameFrom(sheet.name, out.outputFunctionName)
        val ergo = ExcelToErgo.toErgoCode(functionName, sheet, Bindings(outputBindings), sheet.input)
        //val v = sheet.sheet.getValue((out.cellReference.cellCol, out.cellReference.cellRow))
        //println(v)
        lines.appendAll(Seq(ErgoLine("")))
        lines.appendAll(ergo.lines)
      }
      ErgoCode(lines.toSeq)
    }



    val codeStr: Seq[String] = {
      for (c <- ergoCode;
           c2 <- c.code) yield c2.line
    }

    val inputBindings = Seq[String]()

    val clauseInputBindings = for (b <- codeGen.calculations.inputBindings.zipWithIndex) yield {
      s"    let input${b._2 + 1}: Double = request.${b._1.modelFieldName};"
    }

    val mapFieldNameToIndex = (for (b <- codeGen.calculations.inputBindings.zipWithIndex) yield (b._1.modelFieldName, b._2+1)).toMap

    println("All model field names ----")
    for (z <- codeGen.calculations.sheets;
         k <- z.input) {
      println(k.modelFieldReference.modelFieldName)
    }
    println("---")
    val seqInputs = for (z <- codeGen.calculations.sheets.head.input) yield z.modelFieldReference.modelFieldName

    val clauseOutputBindings = for (b <- codeGen.calculations.outputBindings.zipWithIndex) yield {
      val name = b._1.ioSheet.name
      val outputName = b._1.cellOutput.outputFunctionName

      val names = (for (i <- seqInputs) yield {
        "input" + mapFieldNameToIndex(i)
      }).mkString(", ")

      val modelFieldNames = for (k <- b._1.ioSheet.input) yield {
        "input" + mapFieldNameToIndex(k.modelFieldReference.modelFieldName)
      }
      val modelFieldNamesMkstring = modelFieldNames.mkString(", ")

      s"    let output${b._2 + 1}: Double = spreadsheetFunction_${name}_${outputName}(${modelFieldNamesMkstring});"
    }

    val responseOutputBindings = for (b <- codeGen.calculations.outputBindings.zipWithIndex) yield {
      s"      ${b._1.fieldNameInTheOutput}: output${b._2+1}"
    }

    val codeSeq = Seq(s"namespace ${codeGen.calculations.namespace}",
      "") ++ codeGen.calculations.importPackages.map((p) => "import " + p) ++
      codeStr ++
      Seq("") ++
      Seq(s"contract ${codeGen.calculations.contractName} over X {",
        "")  ++ inputBindings ++ Seq(
      "  // Clause",
      s"  clause helloworld(request: ${codeGen.calculations.requestClassName}): ${codeGen.calculations.responseClassName} {") ++
      clauseInputBindings ++
      clauseOutputBindings ++
      Seq(
        s"    return ${codeGen.calculations.responseClassName} {") ++
      responseOutputBindings ++
      Seq("    }",
        "  }",
        "}",
        "")

    codeSeq.mkString("\n")

  }
}
