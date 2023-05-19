package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

val enums = {

  enum Color(code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  case class Cat(
      name: String,
      age: Int,
      @EnumValues(Color.values)
      color: Color
  )

  val eitherVar = Var(
    Cat("Scala", 10, Color.White)
  )

  div(
    child <-- eitherVar.signal.map { item =>
      div(
        s"$item"
      )
    },
    Form.renderVar(eitherVar)
  )
}
