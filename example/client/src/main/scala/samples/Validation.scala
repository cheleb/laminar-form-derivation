package samples

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.forms.given

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Positive

case class APositif(
    optional: Option[String],
    optionalInt: Option[Int],
    doublePositive: Double :| Positive,
    optionalDoublePositive: Option[Double :| Positive]
)

val aPositifVar = Var(APositif(Some("name"), Some(1), 1, Some(1)))

val validation = div(
  child <-- aPositifVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(aPositifVar)
)
