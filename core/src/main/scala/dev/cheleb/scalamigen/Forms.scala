package dev.cheleb.scalamigen.forms

import dev.cheleb.scalamigen.*

import io.github.iltotore.iron.{given, *}
import io.github.iltotore.iron.constraint.all.{given, *}

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*

given Defaultable[Int] with
  def default = 0

given Defaultable[String] with
  def default = ""

given Defaultable[IronType[Double, Positive]] with
  def default = 1.0.refine[Positive]

given Form[IronType[Double, Positive]] with
  def render(
      variable: Var[IronType[Double, Positive]],
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
          str.toDoubleOption match
            case None =>
              errorVar.set("Not a number")
            case Some(double) =>
              double.refineEither[Positive] match
                case Left(error) =>
                  errorVar.set(error)
                case Right(value) =>
                  errorVar.set("")
                  variable.set(value)
        }
      )
    )
