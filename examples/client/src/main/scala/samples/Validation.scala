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

  given Defaultable[Double :| GreaterEqual[8.0]] with
    def default: Double :| GreaterEqual[8.0] = 8.0

  given IronTypeValidator[Double, GreaterEqual[8.0]] =
    _.toDoubleOption match
      case None         => Left("Not a number")
      case Some(double) => double.refineEither[GreaterEqual[8.0]]

  val ironSampleVar = Var(
    IronSample(CurrencyCode("Eur"), Some("name"), Some(1), 9.1, Some(1))
  )

  val errorBus = ironSampleVar.errorBus

  Sample(
    "Validation",
    div(
      ironSampleVar.asForm(errorBus),
      div(
        child <-- errorBus.watch
          .map { errors =>
            div(
              errors.collect {
                case (field, ValidationStatus.Invalid(message, true)) =>
                  div(
                    s"$field: $message"
                  )
              }.toSeq
            )
          }
      )
    ),
    div(
      child <-- ironSampleVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """|
       |given Form[CurrencyCode] = stringForm(CurrencyCode(_))
       |
       |  case class IronSample(
       |      curenncyCode: CurrencyCode,
       |      optional: Option[String],
       |      optionalInt: Option[Int],
       |      doubleGreaterThanEight: Double :| GreaterEqual[8.0],
       |      optionalDoublePositive: Option[Double :| Positive]
       |  )
       |
       |  given Defaultable[Double :| GreaterEqual[8.0]] with
       |    def default: Double :| GreaterEqual[8.0] = 8.0
       |
       |  given Validator[IronSample] with
       |    def isValid(a: IronSample): Boolean =
       |      true
       |
       |  given IronTypeValidator[Double, GreaterEqual[8.0]] =
       |    _.toDoubleOption match
       |      case None         => Left("Not a number")
       |      case Some(double) => double.refineEither[GreaterEqual[8.0]]
       |
       |  val ironSampleVar = Var(
       |    IronSample(CurrencyCode("Eur"), Some("name"), Some(1), 9.1, Some(1))
       |  )
""".stripMargin
  )

}
