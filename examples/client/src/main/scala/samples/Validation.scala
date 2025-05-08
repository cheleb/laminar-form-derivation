package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

def validation(using
    wf: WidgetFactory
): Sample = {

  case class LatLon(lat: Double, lon: Double) {
    override def toString: String = s"$lat,$lon"
  }

  given Form[CurrencyCode] = stringForm(CurrencyCode(_))

  given Form[LatLon] = stringFormWithValidation(using
    new Validator[LatLon] {
      override def validate(value: String): Either[String, LatLon] = {
        value.split(",") match {
          case Array(lat, lon) =>
            (
              lat.toDoubleOption.toRight("Invalid latitude"),
              lon.toDoubleOption.toRight("Invalid longitude")
            ) match {
              case (Right(lat), Right(lon)) => Right(LatLon(lat, lon))
              case (Left(latError), Left(rightError)) =>
                Left(s"$latError and $rightError")
              case (Left(latError), _) => Left(latError)
              case (_, Left(lonError)) => Left(lonError)
            }
          case _ => Left("Invalid format")
        }
      }
    }
  )

  case class IronSample(
      curenncyCode: CurrencyCode,
      optional: Option[String],
      optionalInt: Option[Int],
      doubleGreaterThanEight: Double :| GreaterEqual[8.0],
      optionalDoublePositive: Option[Double :| Positive],
      latLong: LatLon
  )

  val ironSampleVar = Var(
    IronSample(
      CurrencyCode("Eur"),
      Some("name"),
      Some(1),
      9.1,
      Some(1),
      LatLon(1, 2)
    )
  )

  Sample(
    "Validation",
    div(
      ironSampleVar.asForm
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
       |  val ironSampleVar = Var(
       |    IronSample(CurrencyCode("Eur"), Some("name"), Some(1), 9.1, Some(1))
       |  )
""".stripMargin
  )

}
