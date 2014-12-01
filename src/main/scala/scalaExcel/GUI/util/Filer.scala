package scalaExcel.GUI.util

import java.io.File

import scala.io._
import scala.collection.immutable
import scalaExcel.GUI.data.{LabeledDataTable, DataCell}
import LabeledDataTable.DataTable
import scalaExcel.GUI.data.DataCell
import scalaExcel.model.Sheet

/**
 * Created by Chris on 10-11-2014.
 *
 * The object that handles saving and loading from the file system
 */
object Filer {

//  def cellToCSV(cell: DataCell) : String =  cell.expression
//  def stringToCSV(cell: String) : String = cell
//
//  def rowToCSV[T](v : (T => String))(row : Traversable[T]) : String =
//    row.foldLeft("")((csv, cell) => csv + v(cell) + ",")
//
//  def gridToCSV[T](v: (T => String))( grid: Traversable[Traversable[T]]) : String =
//    grid.foldLeft("")((csv, row) => csv + rowToCSV(v)(row) + "\n")
//
//
//  def save[T](formatter: (Traversable[Traversable[T]])=>String)
//             (file: File, grid: Traversable[Traversable[T]]) = {
//    printToFile(file) { _.print(formatter(grid)) }
//  }
//
//
//  def toStringTable(dataTable: DataTable) : Traversable[Traversable[String]] =
//    dataTable.map(_.map(_.value.expression))
//
//  def saveCSV(file: java.io.File, dataTable: DataTable) = {
//    val data = toStringTable(dataTable)
//    Filer.save[String](Filer.gridToCSV(identity))(file, data)
//  }
//

  /** Save sheet as CSV file */
  def saveCSV(file: java.io.File, sheet: Sheet) = {
    printToFile(file)(_ print sheetToCSV(sheet))
  }

  /** Convert sheet to CSV string */
  def sheetToCSV(sheet: Sheet) : String = {
    val rowSize = sheet.values.maxBy{case ((r,c),_) => r}._1._1
    val columnSize = sheet.values.maxBy{case ((r,c),_) => c}._1._2

    (0 to rowSize).map(row => {
      (0 to columnSize).map(column => sheet.valueAt((column, row))).map({
        case Some(v) => v.toString
        case _ => ""
      }).foldRight("")((fold, v) => fold + "," + v)
    }).foldRight("")((fold, row) => fold + "\n" + row)
  }

  /** Covert CSV string to Sheet */
  def CSVToSheet(csv: Array[Array[String]]) : Sheet = {
    val indexed = csv.zipWithIndex.flatMap{case (row, rowIndex) =>
      row.zipWithIndex.map { case (value, colIndex) => ((rowIndex, colIndex), value) }
    }
    indexed.foldLeft(new Sheet()){case (sheet, ((r, c), value)) =>
      val (s, updates) = sheet.setCell((c, r), value)
      s
    }
  }

  /** Get the contents of a csv file */
  def loadCSV(file: java.io.File) : Sheet = {
    val linesOfTokens = Source.fromFile(file).getLines()
      .map(line => line.split(",").toArray)
      .toArray
    CSVToSheet(linesOfTokens)
  }

  /** Generic printer to file */
  private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) = {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }



}
