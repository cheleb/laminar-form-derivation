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

  val errorBus = new EventBus[(String, ValidationEvent)]

  given Validator[IronSample] with
    def isValid(a: IronSample): Boolean =
      true

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
      ironSampleVar.asForm(errorBus),
      child <-- ironSampleVar.isValid.map { valid =>
        div(
          div(
            s"Valid: $valid"
          )
        )
      },
      div(
        child <-- errorBus.events
          .scanLeft(Map.empty[String, ValidationStatus]) {
            case (acc, (field, event)) =>
              event match
                case ValidEvent => acc - field
                case InvalideEvent(error) =>
                  acc + (field -> ValidationStatus.Invalid(error, true))
                case HiddenEvent =>
                  acc.get(field) match
                    case Some(ValidationStatus.Invalid(message, true)) =>
                      acc + (field -> ValidationStatus.Invalid(message, false))
                    case _ => acc
                case ShownEvent =>
                  acc.get(field) match
                    case Some(ValidationStatus.Invalid(message, false)) =>
                      acc + (field -> ValidationStatus.Invalid(message, true))
                    case _ => acc

          }
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
    )
  )

}
