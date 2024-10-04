package samples

import dev.cheleb.scalamigen.*
import com.raquo.laminar.api.L.*

val either = {

  case class Cat(name: String, age: Int)
  case class Dog(name: String, age: Int)
  given Defaultable[Cat] with
    def default = Cat("", 0)

  given Defaultable[Dog] with
    def default = Dog("", 0)

  @Panel("Either", false)
  case class EitherSample(
      either: Either[Cat, Dog],
      primitiveEither: Either[Cat, String],
      optionalInt: Option[Int]
  )

  val eitherVar = Var(
    EitherSample(
      Left(Cat("Scala le chat", 6)),
      Right("Forty two"),
      Some(1)
    )
  )
  Sample(
    "Either",
    eitherVar.asForm,
    div(child <-- eitherVar.signal.map { item =>
      div(
        s"$item"
      )
    }),
    """|
         |@Panel("Either", false)
         |case class EitherSample(
         |    either: Either[Cat, Dog],
         |    primitiveEither: Either[Cat, String],
         |    optionalInt: Option[Int]
  )""".stripMargin
  )

}
