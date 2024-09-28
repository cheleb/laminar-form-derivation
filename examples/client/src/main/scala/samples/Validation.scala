package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

val validation = {

  given Form[CurrencyCode] = stringForm(CurrencyCode(_))

  case class IronSample(
      curenncyCode: CurrencyCode,
      optional: Option[String],
      optionalInt: Option[Int],
      doubleGreaterThanEight: Double :| GreaterEqual[8.0],
      optionalDoublePositive: Option[Double :| Positive]
  )

  given Validator[IronSample] = new Validator[IronSample] {
    override def isValid(a: IronSample): Boolean =
      a.optional.isDefined
  }

  given IronTypeValidator[Double, GreaterEqual[8.0]] =
    _.toDoubleOption match
      case None         => Left("Not a number")
      case Some(double) => double.refineEither[GreaterEqual[8.0]]

  val ironSampleVar = Var(
    IronSample(CurrencyCode("Eur"), Some("name"), Some(1), 9.1, Some(1))
  )

  Sample(
    "Validation",
    div(
      child <-- ironSampleVar.signal.map { item =>
        div(
          s"$item"
        )
      },
      ironSampleVar.asForm,
      child <-- ironSampleVar.isValid.map { valid =>
        div(
          s"Valid: $valid"
        )
      }
    )
  )
}
