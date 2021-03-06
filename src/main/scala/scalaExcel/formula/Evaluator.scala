
package scalaExcel.formula

import scalaExcel.CellPos

import math.{min, max, pow, abs, round}

object Evaluator {

  type Ctx = ACell => Value

  // for most simple operations, no ranges and errors are valid
  def evalIfValidOperand(ctx: Ctx, e: Expr, f: Value => Value) =
    e match {
      case r: ARange => VErr(InvalidValue)
      case _ => evalIn(ctx, e) match {
        case err: VErr => err
        case v => f(v)
      }
    }

  // reduce a list of expressions to a single value
  def reduce(ctx: Ctx, f: ((Value, Value) => Value), r: Value, args: List[Expr]): Value =
    r match {
      case e: VErr => e
      case _ => args match {
        case x :: xs => evalIfValidOperand(ctx, x, v => reduce(ctx, f, f(r, v), xs))
        case _ => r
      }
    }

  // combine two expressions to a single value
  def reduce2(ctx: Ctx, f: ((Value, Value) => Value), lhs: Expr, rhs: Expr) =
    evalIfValidOperand(ctx, lhs, l => reduce(ctx, f, l, List(rhs)))

  // Apply a function to one double value
  def applyToDouble(f: (Double => Double))(v: Value): Value = v match {
    case VDouble(d) => VDouble(f(d))
    case VBool(b)   => applyToDouble(f)(boolToVDouble(b))
    case VEmpty     => VDouble(f(0))
    case _          => VErr(InvalidValue)
  }

  // Apply a function to combine two double values
  def applyToDoubles(f: (Double, Double) => Double)(lhs: Value, rhs: Value): Value = (lhs, rhs) match {
    case (VDouble(l), VDouble(r)) => VDouble(f(l, r))
    case (VBool(b), v)            => applyToDoubles(f)(boolToVDouble(b), v)
    case (v, VBool(b))            => applyToDoubles(f)(v, boolToVDouble(b))
    case (VEmpty, v)              => applyToDoubles(f)(VDouble(0), v)
    case (v, VEmpty)              => applyToDoubles(f)(v, VDouble(0))
    case es => pickError(es, InvalidValue)
  }

  def pickError(t: (Value, Value), default: ErrType) = t match {
    case (e: VErr, _) => e
    case (_, e: VErr) => e
    case _ => VErr(default)
  }

  // Casting of booleans to a double Value doubles
  def boolToVDouble(b: Boolean) = VDouble(if (b) 1.0 else 0.0)

  def boolToString(b: Boolean) = b.toString.toUpperCase

  def doubleToString(d: Double) = if (d.isValidInt) d.toInt.toString else d.toString

  def doubleToVBool(d: Double) = VBool(d != 0)

  def emptyToZero(v: Value) = v match {
    case VEmpty => VDouble(0)
    case _ => v
  }

  def valueToVBool(v: Value) = v match {
    case b: VBool => b
    case VDouble(d) => doubleToVBool(d)
    case VEmpty => VBool(false)
    case x => x
  }

  def valueToVString(v: Value) = v match {
    case VBool(b) => VString(boolToString(b))
    case VDouble(d) => VString(doubleToString(d))
    case VEmpty => VString("")
    case x => x
  }

  // Mainly desugar references

  def desugarArgs(l: List[Expr]): List[Expr] = l match {
    case x :: xs => (desugar(x) match {
      case ARange(cs) => cs
      case y => List(y)
    }) ++ desugarArgs(xs)
    case _ => List()
  }

  def desugar(e: Expr): Expr = e match {
    case c: Cell => desugarCell(c)
    case Range(c1, c2) => desugarRange(c1, c2)
    case Group(e) => desugar(e)
    case _ => e
  }

  def desugarCell(c: Cell) = c match {
    case Cell(ColRef(c, _), RowRef(r, _)) => ACell((c, r))
  }

  def desugarRange(c1: Cell, c2: Cell): Expr =
    (c1, c2) match {
      case (
        Cell(ColRef(c1, _), RowRef(r1, _)),
        Cell(ColRef(c2, _), RowRef(r2, _))
      ) =>
        if (c1 == c2 && r1 == r2)
          ACell((c1, r1))
        else {
          val rs = List.range(r1, r2 + 1)
          val cs = List.range(c1, c2 + 1)
          ARange(for (r <- rs; c <- cs) yield ACell((c,r)))
        }
  }

  // top level eval, returns error for ranges
  def eval(ctx: Ctx, e: Expr) =
    desugar(e) match {
      case ARange(_) => VErr(InvalidValue)
      case Const(VEmpty) => VString("")
      case x => evalIn_(ctx, x) match {
        case VEmpty => VDouble(0)
        case v => v
      }
    }

  // internal eval with desugaring
  def evalIn(ctx: Ctx, e: Expr): Value =
    evalIn_(ctx, desugar(e))

  // internal eval without desugaring
  def evalIn_(ctx: Ctx, e: Expr): Value =
    e match {
      case Const(c) => c
      case BinOp(op, lhs, rhs) => evalBinOp(ctx, op, desugar(lhs), desugar(rhs))
      case UnOp(op, v) => evalUnOp(ctx, op, desugar(v))
      case Call(f, args) => evalCall(ctx, f, args)
      case c: ACell => ctx(c)
      case _ => VErr(NA)
    }

  def evalBinOp(ctx: Ctx, op: Op2, lhs: Expr, rhs: Expr) = op match {
    case Eq()     => reduce2(ctx, boolEq, lhs, rhs)
    case GT()     => reduce2(ctx, boolGt, lhs, rhs)
    case LT()     => reduce2(ctx, boolLt, lhs, rhs)
    case GTE()    => reduce2(ctx, boolGte, lhs, rhs)
    case LTE()    => reduce2(ctx, boolLte, lhs, rhs)
    case NEq()    => reduce2(ctx, boolNeq, lhs, rhs)
    case Concat() => reduce2(ctx, concat, lhs, rhs)
    case Plus()   => reduce2(ctx, applyToDoubles(_ + _), lhs, rhs)
    case Minus()  => reduce2(ctx, applyToDoubles(_ - _), lhs, rhs)
    case Mul()    => reduce2(ctx, applyToDoubles(_ * _), lhs, rhs)
    case Div()    => reduce2(ctx, doubleDiv, lhs, rhs)
    case Expon()  => reduce2(ctx, doubleExpon, lhs, rhs)
  }

  def evalUnOp(ctx: Ctx, op: Op1, v: Expr) = op match {
    // Unary Plus is a no-op in Excel
    case Plus()    => eval(ctx, v)
    case Minus()   => applyToDouble(- _)(eval(ctx, v))
    case Percent() => applyToDouble(_ / 100)(eval(ctx, v))
  }

  def concat(lhs: Value, rhs: Value): Value = (valueToVString(lhs), valueToVString(rhs)) match {
    case (VString(s1), VString(s2)) => VString(s1 + s2)
    case es => pickError(es, NA)
  }

  private def boolCmp[T](cmp: (Int => T))(lhs: Value, rhs: Value) =
    cmp(emptyToZero(lhs).compare(emptyToZero(rhs)))

  def boolEq  = boolCmp(c => VBool(c == 0)) _
  def boolNeq = boolCmp(c => VBool(c != 0)) _
  def boolGt  = boolCmp(c => VBool(c > 0)) _
  def boolGte = boolCmp(c => VBool(c >= 0)) _
  def boolLt  = boolCmp(c => VBool(c < 0)) _
  def boolLte = boolCmp(c => VBool(c <= 0)) _

  def doubleDiv(lhs: Value, rhs: Value) = applyToDoubles(_ / _)(lhs, rhs) match {
    case VDouble(d) if (d.isInfinity) => VErr(DivBy0)
    case x => x
  }

  def doubleExpon(base: Value, expon: Value): Value = (base, expon) match {
    case (VDouble(0), VDouble(0)) => VErr(NotNumeric)
    case (b, e) => applyToDoubles(pow)(base, expon) match {
      case VDouble(d) => if (d.isInfinity) VErr(DivBy0)
                         else if (d.isNaN) VErr(NotNumeric)
                         else VDouble(d)
      case x => x
    }
    case _ => VErr(InvalidValue)
  }

  def evalCall(ctx: Ctx, fn: String, args: List[Expr]) =
    fn match {
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
      case _ => VErr(InvalidName)
    }

  // Numeric functions

  def evalCallAverage(ctx: Ctx, args: List[Expr]) =
    evalIn(ctx, BinOp(Div(), Call("SUM", args), Call("COUNT", args)))

  def evalCallPower(ctx: Ctx, args: List[Expr]) = args match {
    case List(num, exp) => evalIn(ctx, BinOp(Expon(), num, exp))
    case _ => VErr(FunctionsArgs)
  }

  def evalCallRound(ctx: Ctx, args: List[Expr]): Value = args match {
    case List(num) => evalCallRound(ctx, List(num, Const(VDouble(0))))
    case List(num, digits) => (args map (evalIn(ctx, _))) match {
      case List(VDouble(num), VDouble(dig)) => {
        val s = pow(10, dig.toInt)
        VDouble(round(num * s) / s)
      }
      case _ => VErr(InvalidValue)
    }
    case _ => VErr(FunctionsArgs)
  }

  // Cell functions

  private def getRangeBounds(e: Cell) = e match {
    case Cell(ColRef(c, _), RowRef(r, _)) => ((c, r), (c, r))
  }

  private def getRangeBounds(e: Range) = e match {
    case Range(Cell(ColRef(c1, _), RowRef(r1, _)), Cell(ColRef(c2, _), RowRef(r2, _))) =>
      ((min(c1, c2), min(r1, r2)), (max(c1, c2), max(r1, r2)))
  }

  private def execForRangeArg(f: (CellPos, CellPos) => Value)(args: List[Expr]) = args match {
    case List(c: Cell)  => f tupled getRangeBounds(c)
    case List(r: Range) => f tupled getRangeBounds(r)
    case _ => VErr(FunctionsArgs)
  }

  def evalCallRow     = execForRangeArg((c1, c2) => VDouble(c1._2 + 1)) _

  def evalCallRows    = execForRangeArg((c1, c2) => VDouble(c2._2 - c1._2 + 1)) _

  def evalCallColumn  = execForRangeArg((c1, c2) => VDouble(c1._1 + 1)) _

  def evalCallColumns = execForRangeArg((c1, c2) => VDouble(c2._1 - c1._1 + 1)) _

  def evalCallCount(ctx: Ctx, args: List[Expr]) =
    VDouble(args
      .map(arg => evalIn(ctx, arg) match {
        case VDouble(_) => 1
        case _ => 0
      })
      .fold(0)(_+_))

  def evalCallMatch(ctx: Ctx, args: List[Expr]): Value = args match {
    // value, array, type
    // Default to exact match type = 0 (1 in excel), but 0 makes most sense
    // Type 1: largest value that is >= v
    // Type 0: exactly v
    // Type -1: smallest value <= v
    case List(v, a)           => evalCallMatch(ctx, List(v, a, Const(VDouble(0))))
    case List(v, c: Cell, t)  => evalCallMatch(ctx, List(v, Range(c, c), t))
    case List(v, r: Range, t) => {

      val ((c1, r1), (c2, r2)) = getRangeBounds(r)

      val positions =
        if (r1 == r2) (c1 to c2) map {(_, r1)}
        else if (c1 == c2) (r1 to r2) map {(c1, _)}
        else List()

      lazy val value = evalIn(ctx, v)
      val pos = positions.zipWithIndex.find {
        case (p, i) => value == ctx(ACell(p))
      }

      pos match {
        case Some((p, i)) => VDouble(i + 1)
        case None => VErr(NA)
      }

    }
    case List(v, a, t) => VErr(InvalidValue)
    case _ => VErr(FunctionsArgs)
  }

  def evalCallVLookUp(ctx: Ctx, args: List[Expr]): Value = args match {
    // value, array, index, exact
    // default exact to FALSE, which is the only type we implement here.
    case List(v, a, i)           => evalCallVLookUp(ctx, args :+ Const(VBool(false)))
    case List(v, c: Cell, i, e)  => evalCallVLookUp(ctx, List(v, Range(c, c), i, e))
    case List(v, r: Range, i, e) => evalIn(ctx, i) match {
      case VDouble(index) => {
        val ((c1, r1), (c2, r2)) = getRangeBounds(r)
        lazy val value = evalIn(ctx, v)
        val lookupCol = c1 + index.toInt - 1
        val row = (r1 to r2) find { ri => value == ctx(ACell((c1, ri))) }
        row match {
          case Some(r) =>
            if (lookupCol > c2) VErr(InvalidRef)
            else ctx(ACell((lookupCol, r)))
          case None => VErr(NA)
        }
      }
      case _ => VErr(InvalidValue)
    }
    case List(v, a, i, e) => VErr(InvalidValue)
    case _ => VErr(FunctionsArgs)
  }

  def evalCallAddress(ctx: Ctx, args: List[Expr]): Value = args match {
    case List(row, col) => evalCallAddress(ctx, List(row, col, Const(VDouble(1))))
    case List(row, col, abs) => (args map (evalIn(ctx, _))) match {
      case List(VDouble(row), VDouble(col), VDouble(abs))
        if (row >= 1 && col >= 1 && abs >= 1 && abs <= 4) => VString(
          "" ++ (if (abs == 1 || abs == 3) "$" else "") ++
          numToCol(col.toInt - 1) ++
          (if (abs == 1 || abs == 2) "$" else "") ++
          row.toInt.toString
        )
      case List(r, c, a) => VErr(InvalidValue)
    }
    case _ => VErr(FunctionsArgs)
  }

  // Logical functions

  def evalCallIf(ctx: Ctx, args: List[Expr]) = (args match {
    // Normalize length of arguments to 3
    case List(condition) => List(condition, Const(VBool(true)), Const(VBool(false)))
    case List(condition, trueValue) => List(condition, trueValue, Const(VBool(false)))
    case x => x
  }) match {
    // evaluate the condition and then either the true or left value
    case List(condition, trueValue, falseValue) => valueToVBool(evalIn(ctx, condition)) match {
      case VBool(b) => evalIn(ctx, if (b) trueValue else falseValue)
      case _ => VErr(InvalidValue)
    }
    case _ => VErr(FunctionsArgs)
  }

  def evalCallOr(ctx: Ctx, args: List[Expr]) = {
    args.foldLeft[Value](VBool(false))((res, arg) => res match {
      case VBool(true) => res
      case _ => valueToVBool(evalIn(ctx, arg))
    })
  }

  def evalCallAnd(ctx: Ctx, args: List[Expr]) = {
    args.foldLeft[Value](VBool(true))((res, arg) => res match {
      case VBool(false) => res
      case _ => valueToVBool(evalIn(ctx, arg))
    })
  }

  def evalCallNot(ctx: Ctx, args: List[Expr]) = args match {
    case List(arg) => valueToVBool(evalIn(ctx, arg)) match {
      case VBool(v) => VBool(!v)
      case _ => VErr(InvalidValue)
    }
    case _ => VErr(FunctionsArgs)
  }

  // String functions

  def evalWithString(f: String => Value)(ctx: Ctx, args: List[Expr]) = {
    args match {
      case List(value) => valueToVString(evalIn(ctx, value)) match {
        case VString(str) => f(str)
        case x => x
      }
      case _ => VErr(FunctionsArgs)
    }
  }

  def evalCallUpper = evalWithString(str => VString(str.toUpperCase)) _

  def evalCallLower = evalWithString(str => VString(str.toLowerCase)) _

  def evalCallLen = evalWithString(str => VDouble(str.length)) _

  def evalCallTrim = evalWithString(str => VString(str.trim)) _

  def evalCallTrunc = evalWithString(str => VString(str.toDouble.toInt.toString)) _

  // IS functions

  def evalIsTrueFor(f: Value => Boolean)(ctx: Ctx, args: List[Expr]) = args match {
    case List(value) => VBool(f(evalIn(ctx, value)))
    case _ => VBool(false)
  }

  def evalCallIsBlank = evalIsTrueFor {
    case VEmpty => true
    case _ => false
  } _

  def evalCallIsError = evalIsTrueFor {
    case VErr(e) => true
    case _ => false
  } _

  def evalCallIsNA = evalIsTrueFor {
    case VErr(NA) => true
    case _ => false
  } _

  def evalCallIsLogical = evalIsTrueFor {
    case VBool(_) => true
    case _ => false
  } _

  def evalCallIsNumber = evalIsTrueFor {
    case VDouble(_) => true
    case _ => false
  } _

  def evalCallIsText = evalIsTrueFor {
    case VString(_) => true
    case _ => false
  } _

}
