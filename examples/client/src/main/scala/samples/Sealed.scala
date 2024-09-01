package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

val sealedClasses = {
  sealed trait Animal
  case class Horse(name: String, age: Int) extends Animal
  case class Lama(name: String, age: Int, splitDistance: Int) extends Animal

  case class Owner(name: String, pet: Animal)

  Sample(
    "Sealed", {

      val eitherVar = Var(Owner("Agnes", Horse("Niram <3", 6)))

      div(
        child <-- eitherVar.signal.map { item =>
          div(
            s"$item"
          )
        },
        child <-- eitherVar.signal
          .distinctByFn((old, nw) => old.pet.getClass == nw.pet.getClass)
          .map { item =>
            eitherVar.asForm
          },
        div(
          button(
            "Change to Lama",
            onClick.mapToUnit --> (_ =>
              eitherVar.update(_.copy(pet = Lama("Lama", 3, 2)))
            )
          ),
          button(
            "Change to Horse",
            onClick.mapToUnit --> (_ =>
              eitherVar.update(_.copy(pet = Horse("Niram", 13)))
            )
          )
        )
      )
    }
  )
}
