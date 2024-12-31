package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import java.util.UUID

val enums = {
  enum Color(val code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  given colorForm: Form[Color] =
    selectForm(Color.values, labelMapper = c => s"$c ${c.code}")

  case class Meal(id: UUID, name: String)

  val allMeals = List(
    Meal(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Pizza"),
    Meal(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Pasta")
  )

  given mealForm: Form[UUID] =
    selectMappedForm(allMeals, mapper = m => m.id, labelMapper = _.name)

  case class Basket(color: Color, cat: Cat)

  case class Cat(
      name: String,
      weight: Int :| Positive,
      kind: Boolean = true,
      color: Color,
      mealId: UUID
  )
//  case class Dog(name: String, weight: Int)

  val enumVar = Var(
    Basket(
      Color.Black,
      Cat(
        "Scala",
        10,
        true,
        Color.White,
        UUID.fromString("00000000-0000-0000-0000-000000000000")
      )
    )
  )

  Sample(
    "Enums",
    enumVar.asForm(enumVar.errorBus),
    div(
      child <-- enumVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """|
       |enum Color(val code: String):
       |  case Black extends Color("000")
       |  case White extends Color("FFF")
       |  case Isabelle extends Color("???")
       |
       |given colorForm: Form[Color] =
       |  selectForm(Color.values, labelMapper = c => s"$c ${c.code}")
       |
       |case class Meal(id: UUID, name: String)
       |
       |val allMeals = List(
       |  Meal(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Pizza"),
       |  Meal(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Pasta")
       |)
       |
       |given mealForm: Form[UUID] =
       |  selectMappedForm(allMeals, mapper = m => m.id, labelMapper = _.name)
       |
       |case class Basket(color: Color, cat: Cat)
       |
       |case class Cat(
       |  name: String,
       |  weight: Int :| Positive,
       |  kind: Boolean = true,
       |  color: Color,
       |  mealId: UUID
       |)
       |""".stripMargin
  )
}
