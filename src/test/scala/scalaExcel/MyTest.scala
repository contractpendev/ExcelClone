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
    val outputs = mutable.Buffer[Seq[CellOutput]]()

    sheets += new Sheet()
      .setCell( (0,0), "100")
      .setCell( (0,1), "50")
      .setCell( (0,2), "=A1+A2")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("field2", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 0, 1), DataModelFieldReference("field1", "ClassName", "package.name")))
    outputs += Seq(CellOutput("nameOutput1", CellReference(DataTypeDouble, 0, 2), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "1")
      .setCell((1, 0), "=2+2/0.5+(1.2/2.0)")
      .setCell((0, 1), "=A1+1")
      .setCell((1, 1), "=$A$1+1")
      .setCell((3, 0), "=B1/0.5")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 1, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("nameOutput2", CellReference(DataTypeDouble, 3, 0), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "1")
      .setCell((1, 0), "2")
      .setCell((0, 1), "=A1*1")
      .copyCell((0, 1), (1, 1))

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 1, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellOutput("nameOutput3", CellReference(DataTypeDouble, 1, 1), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "0")
      .setCell((0, 1), "2")
      .setCell((0, 2), "=A1+A2+5+6") // 1
      .setCell((0, 3), "=A2*A3") // 2
      .setCell((0, 4), "=A3/A4") // 3
      .setCell((0, 5), "=A4+A5") // 5

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")),
      CellInput("name2", CellReference(DataTypeDouble, 0, 1), DataModelFieldReference("fieldName", "ClassName", "package.name"))
    )

    outputs += Seq(CellOutput("any", CellReference(DataTypeDouble, 0, 5), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "100")
      .setCell((0, 1), "2")
      .setCell((0, 2), "=A1^A2")
      .setCell((0, 3), "=A3")

    inputs += Seq(CellInput("name1", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "100")
      .setCell((0, 1), "101")
      .setCell((0, 2), "=A1 <> A2")
      .setCell((0, 3), "=A3")

    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))


    sheets += new Sheet()
      .setCell((0, 0), "=95.2%")
      .setCell((0, 1), "=1000.0")
      .setCell((0, 2), "=A1*A2")

    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))

    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 2), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))


    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=SUM (A1,A2,A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=AVERAGE(A1,A2,A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=3")
      .setCell((0, 1), "=2")
      .setCell((0, 3), "=POWER(A1,A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=1.058329234")
      .setCell((0, 1), "=2")
      .setCell((0, 3), "=ROUND(A1,A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=ROW(A2)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))


    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=ROWS(A1:A3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))



    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=COLUMN(C3)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=COLUMNS(C3:D5)")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

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
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

    sheets += new Sheet()
      .setCell((0, 0), "=1")
      .setCell((0, 1), "=2")
      .setCell((0, 2), "=3")
      .setCell((0, 3), "=A1")
    inputs += Seq(CellInput("name", CellReference(DataTypeDouble, 0, 0), DataModelFieldReference("fieldName", "ClassName", "package.name")))
    outputs += Seq(CellOutput("name", CellReference(DataTypeDouble, 0, 3), DataModelFieldReference("fieldNameOutput", "ClassName", "package.name")))

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

  def generateCode(codeGen: ErgoCodeGen): String = {

    val codeStr: Seq[String] = {
      for (c <- codeGen.ergoCode;
           c2 <- c.code) yield c2.line
    }

    val inputBindings = Seq[String]()


    val clauseInputBindings = for (b <- codeGen.calculations.inputBindings.zipWithIndex) yield {
      s"    let input${b._2 + 1}: Double = request.${b._1.modelFieldName};"
    }

    val mapFieldNameToIndex = (for (b <- codeGen.calculations.inputBindings.zipWithIndex) yield (b._1.modelFieldName, b._2+1)).toMap

    val seqInputs = for (z <- codeGen.calculations.sheets.head.input) yield z.modelFieldReference.modelFieldName

    val clauseOutputBindings = for (b <- codeGen.calculations.outputBindings.zipWithIndex) yield {
      val name = b._1.ioSheet.name
      val outputName = b._1.ioSheet.output.head.name
      // @todo These names are wrong, its true that there are 2 names but the names must be input[n]
      // @todo so where is the mapping from inputs to outputs?
      val names = (for (i <- seqInputs) yield {
        "input" + mapFieldNameToIndex(i)
      }).mkString(", ")
      s"    let output${b._2 + 1}: Double = spreadsheetFunction_${name}_${outputName}(${names});"
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

  @Test def testDefaultFormat() {

    val t = ExcelToErgo.getType("1")
    println(t)


    def functionNameFrom(sheetName: String, outputName: String) = {
      "spreadsheetFunction_" + sheetName + "_" + outputName
    }

    val sheets = testSpreadsheets()

    val packages = Seq("org.accordproject.cicero.runtime.*")
    val namespace = "org.accordproject.acceptanceofdelivery"
    val contractName = "HelloWorld"
    val clauseFunctionName = "helloworld"
    val requestClassName = "MyRequest"
    val responseClassName = "MyResponse"

    for (i <- sheets.head.input) yield {
      i.modelFieldReference
    }

    val inputBindings = Seq(DataModelFieldReferenceBinding("field1", "sheet1", "name", "param1"),
      DataModelFieldReferenceBinding("field2", "sheet2", "name", "param2"),
      DataModelFieldReferenceBinding("field3", "sheet3", "name", "param3"))

    val outputBindings = Seq[DataModelFieldReferenceOutput](DataModelFieldReferenceOutput("fieldNameInTheResponse1", sheets(0)))

    val calculations = Calculations(sheets, packages, namespace, contractName, clauseFunctionName, requestClassName, responseClassName,
      inputBindings, outputBindings)

    val ergoCode = for (sheet <- calculations.sheets.take(1)) yield {
      val lines = mutable.Buffer[ErgoLine]()
      //println("")
      //println("Sheet " + sheet.name + " ---------------------------------------------------------------------------------------")
      // For each output we define a function
      for (out <- sheet.output) {
        val ast = sheet.sheet.getCell(out.cellReference.cellCol, out.cellReference.cellRow).AST
        val outputBindings = Seq(Binding(nameFromCell(sheet.name, out.cellReference), out.cellReference.cellCol, out.cellReference.cellRow, ast))
        val functionName = functionNameFrom(sheet.name, out.name)
        val ergo = ExcelToErgo.toErgoCode(functionName, sheet, Bindings(outputBindings), sheet.input)
        //val v = sheet.sheet.getValue((out.cellReference.cellCol, out.cellReference.cellRow))
        //println(v)
        lines.appendAll(Seq(ErgoLine("")))
        lines.appendAll(ergo.lines)
      }
      ErgoCode(lines.toSeq)
      //

      // namespace
      // import statements
      // defined functions

      //contract HelloWorld over HelloWorldClause {
        // Simple Clause
        //clause helloworld(request : MyRequest) : MyResponse {
        //  return MyResponse{ output: "Hello " ++ contract.name ++ " " ++ request.input }
        //}
      //}
    }

    val codeGen = ErgoCodeGen(calculations, ergoCode)

    println("-------------------- generate code")
    println("")
    val code = generateCode(codeGen)
    println(code)





    /*

namespace org.accordproject.servicelevelagreement

import org.accordproject.cicero.runtime.*
import org.accordproject.cicero.contract.*
import org.accordproject.money.MonetaryAmount

// Safety checking
define function toFixed(credit : Double) : Double {
  return max([0.0,floor(credit * 100.00 + 0.5) / 100.00])
}
// Function for Payment Obligations
define function createPaymentObligation(
    acontract:AccordContract,
    serviceProvider:AccordParty,
    serviceConsumer:AccordParty,
    amount: MonetaryAmount
  ) : PaymentObligation {
  return

    PaymentObligation{
      contract: acontract,
      promisor: some(serviceProvider),
      promisee: some(serviceConsumer),
      deadline: none,
      amount: amount,
      description:
        "payment owed by "
          ++ serviceProvider.partyId
          ++ " to " ++ serviceConsumer.partyId
          ++ " for delivery of service downtimes"
    }
  }
contract ServiceLevelAgreement over ServiceLevelAgreementContract {
  clause invoice(request : MonthSummary) : InvoiceCredit emits PaymentObligation {
   // Pre-conditions checking
    enforce contract.availability1 >= 0.0
        and contract.serviceCredit1.doubleValue >= 0.0
        and contract.availability2 >= 0.0
        and contract.serviceCredit2.doubleValue >= 0.0
    else throw ErgoErrorResponse{ message: "Template variables must not be negative." };

    enforce request.monthlyServiceLevel >= 0.0
        and request.monthlyServiceLevel <= 100.0
    else throw ErgoErrorResponse{ message: "A service level must be at least 0% and at most 100%." };
    //
    // Section 3
    //

    let monthlyCredit =
      // Annex 1, schedule - row 2
      if request.monthlyServiceLevel < contract.availability2
      then (contract.serviceCredit2.doubleValue / 100.0) * request.monthlyCharge
      // Annex 1, schedule - row 1
      else if (request.monthlyServiceLevel < contract.availability1)
      then (contract.serviceCredit1.doubleValue / 100.0) * request.monthlyCharge
      else 0.0;

    // Clause 3.3
    let monthlyCredit = min([monthlyCredit, (contract.monthlyCapPercentage  / 100.0 ) * request.monthlyCharge]);

    // Clause 3.4
    let yearlyCreditCap = ( contract.yearlyCapPercentage / 100.0) * (request.last11MonthCharge + request.monthlyCharge);
    let monthlyCredit = min([monthlyCredit, yearlyCreditCap - request.last11MonthCredit]);

    // Payment Obligations
    enforce (monthlyCredit > 0.0)
      else return InvoiceCredit{
        monthlyCredit: 0.0
      };

    let monetaryAmount = MonetaryAmount{
        doubleValue : monthlyCredit,
        currencyCode : contract.serviceCredit1.currencyCode
    };
    emit createPaymentObligation(contract,contract.serviceProvider,contract.serviceConsumer,monetaryAmount);
    return InvoiceCredit{
      monthlyCredit: toFixed(monthlyCredit)
    }
  }
}

     */

    println("")
  }

}
