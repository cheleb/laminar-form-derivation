package samples

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.forms.given
import dev.cheleb.scalamigen.forms.*

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

case class Cat(name: String, weight: Int)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)

case class EitherSample(
    either: Either[Cat, Dog],
    optionalInt: Option[Int]
)

val eitherVar = Var(EitherSample(Left(Cat("Scala", 6)), Some(1)))

val either = div(
  child <-- eitherVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(eitherVar)
)
