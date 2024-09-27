package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

import com.raquo.laminar.api.L

val enums = {
  enum Color(val code: String) derives Form:
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  case class Basket(color: Color, cat: Cat) derives Form

  given colorForm: Form[Color] = enumForm(Color.values, Color.fromOrdinal)

  case class Cat(
      name: String,
      age: Int,
      color: Color
  ) derives Form

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
