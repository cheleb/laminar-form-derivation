package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var
import com.raquo.laminar.nodes.ReactiveHtmlElement

val sealedClasses = {
  sealed trait Animal

  case class Horse(name: String, age: Int) extends Animal
  case class Lama(name: String, age: Int, splitDistance: Int) extends Animal
  case class Otter(name: String, age: Int) extends Animal

  case class Owner(name: String, pet: Animal)

  Sample(
    "Sealed", {

      val eitherVar = Var(Owner("Agnes", Horse("Niram <3", 6)))

      case class Switcher(
          name: String,
          button: ReactiveHtmlElement[?]
      )

      object Switcher {
        def apply[A <: Animal](na: => A): Switcher =
          val a = na
          val name = a.getClass.getSimpleName
          Switcher(
            name,
            button(
              name.filter(_.isLetter),
              onClick.mapToUnit --> (_ => eitherVar.update(_.copy(pet = a)))
            )
          )
      }

      val switchers = List(
        Switcher(
          Horse("Niram", 13)
        ),
        Switcher(
          Lama("Lama", 3, 2)
        ),
        Switcher(
          Otter("Otter", 13)
        )
      )

      div(
        child <-- eitherVar.signal.map { item =>
          div(
            s"$item"
          )
        },
        child <-- eitherVar.signal
          .distinctByFn((old, nw) => old.pet.getClass == nw.pet.getClass)
          .map { item =>
            div(
              eitherVar.asForm,
              switchers
                .filterNot(_.name == eitherVar.now().pet.getClass.getSimpleName)
                .map(_.button)
            )
          }
      )

    }
  )
}
