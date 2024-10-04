package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

val enums = {
  enum Color(val code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  given colorForm: Form[Color] = enumForm(Color.values, Color.fromOrdinal)

  case class Basket(color: Color, cat: Cat)

  case class Cat(
      name: String,
      weight: Int :| Positive,
      kind: Boolean = true,
      colol: Color
  )
//  case class Dog(name: String, weight: Int)

  val enumVar = Var(
    Basket(Color.Black, Cat("Scala", 10, true, Color.White))
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
         |  enum Color(val code: String):
         |    case Black extends Color("000")
         |    case White extends Color("FFF")
         |    case Isabelle extends Color("???")
         |
         |  given colorForm: Form[Color] = enumForm(Color.values, Color.fromOrdinal)
         |
         |  case class Basket(color: Color, cat: Cat)
         |
         |  case class Cat(
         |      name: String,
         |      age: Int,
         |      color: Color
         |  )
         |
         |""".stripMargin
  )
}
