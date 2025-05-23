package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import com.raquo.laminar.nodes.ReactiveHtmlElement

def sealedClasses(using
    wf: WidgetFactory
): Sample = {

  enum Color(val code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  given colorForm: Form[Color] = selectForm(Color.values)

  sealed trait Animal

  case class Horse(name: String, age: Int, color: Color) extends Animal

  case class Lama(
      name: String,
      age: Int,
      splitDistance: Int,
      color: Color = Color.Isabelle
  ) extends Animal

  case class Otter(name: String, age: Int) extends Animal

  case class Owner(name: String, pet: Animal)

  val sealedVar = Var(Owner("Agnes", Horse("Niram <3", 6, Color.Isabelle)))

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
          onClick.mapToUnit --> (_ => sealedVar.update(_.copy(pet = a)))
        )
      )
  }

  val switchers = List(
    Switcher(
      Horse("Niram", 13, Color.Black)
    ),
    Switcher(
      Lama("Lama", 3, 2)
    ),
    Switcher(
      Otter("Otter", 13)
    )
  )

  Sample(
    "Sealed",
    div(
      child <-- sealedVar.signal
        .distinctByFn((old, nw) => old.pet.getClass == nw.pet.getClass)
        .map { _ =>
          div(
            sealedVar.asForm,
            switchers
              .filterNot(_.name == sealedVar.now().pet.getClass.getSimpleName)
              .map(_.button)
          )
        }
    ),
    div(child <-- sealedVar.signal.map { item =>
      div(
        s"$item"
      )
    }),
    """|  enum Color(val code: String):
       |    case Black extends Color("000")
       |    case White extends Color("FFF")
       |    case Isabelle extends Color("???")
       |
       |  given colorForm: Form[Color] = selectForm(Color.values)
       |
       |  sealed trait Animal
       |
       |  case class Horse(name: String, age: Int, color: Color) extends Animal
       |
       |  case class Lama(
       |      name: String,
       |      age: Int,
       |      splitDistance: Int,
       |      color: Color = Color.Isabelle
       |  ) extends Animal
       |
       |  case class Otter(name: String, age: Int) extends Animal
       |
       |  case class Owner(name: String, pet: Animal)
       |
       |""".stripMargin
  )
}
