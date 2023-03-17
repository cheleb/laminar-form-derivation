package samples

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.forms.given
import dev.cheleb.scalamigen.forms.*

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

//given Form[IronType[Double, Positive]] = formGen[Double, Positive]

import samples.APositif
import com.raquo.airstream.state.Var
case class APositif(
    optional: Option[String],
    optionalInt: Option[Int],
    doublePositive: Double :| GreaterEqual[8.0],
    optionalDoublePositive: Option[Double :| Positive]
)

given IronTypeValidator[Double, GreaterEqual[8.0]] =
  _.toDoubleOption match
    case None         => Left("Not a number")
    case Some(double) => double.refineEither[GreaterEqual[8.0]]

val aPositifVar = Var(APositif(Some("name"), Some(1), 9.1, Some(1)))

val validation = div(
  child <-- aPositifVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(aPositifVar)
)
