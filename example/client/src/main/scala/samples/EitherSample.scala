package samples

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.forms.given
import dev.cheleb.scalamigen.forms.*

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

case class EitherSample(
    either: Either[Person, String],
    optionalInt: Option[Int]
)

val eitherVar = Var(EitherSample(Left(vlad), Some(1)))

val either = div(
  child <-- eitherVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(eitherVar)
)
