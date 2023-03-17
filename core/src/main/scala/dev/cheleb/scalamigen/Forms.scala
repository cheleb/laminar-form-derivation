package dev.cheleb.scalamigen.forms

import dev.cheleb.scalamigen.*

import io.github.iltotore.iron.{given, *}
import io.github.iltotore.iron.constraint.all.{given, *}

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*

import scala.quoted.*
import com.raquo.airstream.state.Var

given Defaultable[Int] with
  def default = 0

given Defaultable[String] with
  def default = ""

given Defaultable[IronType[Double, Positive]] with
  def default = 1.0.refine[Positive]

given IronTypeValidator[Double, Positive] with
  def validate(a: String): Either[String, IronType[Double, Positive]] =
    a.toDoubleOption match
      case None         => Left("Not a number")
      case Some(double) => double.refineEither[Positive]

given [T, C](using fv: IronTypeValidator[T, C]): Form[IronType[T, C]] =
  new Form[IronType[T, C]] {

    override def fromString(
        str: String,
        variable: Var[IronType[T, C]],
        errorVar: Var[String]
    ): Unit =
      fv.validate(str) match
        case Left(error) =>
          errorVar.set(error)
        case Right(value) =>
          errorVar.set("")
          variable.set(value)
  }
