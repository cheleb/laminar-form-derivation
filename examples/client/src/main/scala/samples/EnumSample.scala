package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val enums = {
  enum Color(val code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  given colorForm: Form[Color] = enumForm(Color.values, Color.fromOrdinal)

  case class Basket(color: Color, cat: Cat)

  case class Cat(
      name: String,
      age: Int,
      color: Color
  )

  val eitherVar = Var(
    Basket(Color.Black, Cat("Scala", 10, Color.White))
  )
  Sample(
    "Enums", {

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
