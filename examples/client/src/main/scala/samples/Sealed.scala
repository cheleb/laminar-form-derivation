package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

sealed trait Animal
case class Horse(name: String, age: Int) extends Animal
case class Lama(name: String, age: Int, splitDistance: Int) extends Animal

case class Owner(name: String, pet: Animal)

val sealedClasses = Sample(
  "Sealed", {

    val eitherVar = Var(Owner("Agnes", Horse("Scala le chat", 6)))

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
