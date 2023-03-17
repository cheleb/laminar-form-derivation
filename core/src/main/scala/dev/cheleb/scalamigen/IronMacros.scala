package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.IronType
import io.github.iltotore.iron.refineEither
import io.github.iltotore.iron.constraint.all.Positive

import scala.quoted.*

import io.github.iltotore.iron.Constraint

transparent inline def formGen[T, C]: Form[IronType[T, C]] =
  ${ formImpl[T, C] }

def formImpl[T: Type, C: Type](using q: Quotes) =
  import q.reflect.*
  val base = TypeRepr.of[T]

  def test(expr: Expr[String]) =
    base.asType match
      // case '[Int]    => '{ $expr.toIntOption }
      // case '[Long]   => '{ $expr.toLongOption }
      // case '[Float]  => '{ $expr.toFloatOption }
      case '[Double] => '{ $expr.toDoubleOption }
//    '{ $expr.toDoubleOption }

  def refineExpr(expr: Expr[Double])(using q: Quotes) =
    import q.reflect.*

    val sss = '{ 1.0.asInstanceOf[Positive] }

//    report.errorAndAbort(sss.asTerm.show)

    TypeApply(
      Select.unique(expr.asTerm, "refineEither"),
      List(TypeTree.of[C])
    ).asExpr

  '{
    new Form[IronType[T, C]] {

      def render(
          variable: Var[IronType[T, C]],
          syncParent: () => Unit
      ): HtmlElement =
        val errorVar = Var("")
        div(
          div(child <-- errorVar.signal.map { item =>
            div(
              s"$item"
            )
          }),
          input(
            tpe("number"),
            // _.showClearIcon := true,
            backgroundColor <-- errorVar.signal.map {
              case "" => "white"
              case _  => "red"
            },
            value <-- variable.signal.map(_.toString()),
            onInput.mapToValue --> { str =>
              // ${ Expr(str) }
              ${ test('str) } match {
                case None =>
                  errorVar.set("Not a number")
                case Some(double) =>
                  ${ refineExpr('double) } match
                    case Left(error) =>
                      errorVar.set("error.asInstanceOf[String]")
                    case Right(value) =>
                      errorVar.set("")
                      variable.set(value.asInstanceOf[IronType[T, C]])
                // double.refineEither[$typeC] // match
                //   case Left(error) =>
                //     errorVar.set(error)
                //   case Right(value) =>
                // errorVar.set("")
                // variable.set(value)
              }
            }
          )
        )

    }
  }

def showExpr(expr: Expr[Boolean])(using Quotes): Expr[String] =
  '{ ??? }
