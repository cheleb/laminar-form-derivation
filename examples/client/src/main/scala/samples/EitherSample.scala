package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

val either = {

  case class EitherSample(
      primitive: Either[String, Int],
      either: Either[Cat, Dog],
      optionalInt: Option[Int]
  )

  val eitherVar = Var(
    EitherSample(Left("Weird"), Left(Cat("Scala le chat", 6)), Some(1))
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
