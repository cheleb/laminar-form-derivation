package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

val either = {

  case class EitherSample(
      either: Either[Cat, Dog],
      primitiveEither: Either[Cat, String],
      optionalInt: Option[Int]
  ) derives Form

  val eitherVar = Var(
    EitherSample(
      Left(Cat("Scala le chat", 6)),
      Right("Forty two"),
      Some(1)
    )
  )
  Sample(
    "Either", {

      div(
        child <-- eitherVar.signal.map { item =>
          div(
            s"$item"
          )
        },
        eitherVar.asForm
      )
    }
  )
}
