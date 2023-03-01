package listelements

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.forms.given

import com.raquo.laminar.api.L.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.Positive

case class Person(name: String, age: Int)

case class ListElement(
    ints: List[Person]
)

val listPersonVar = Var(ListElement(List(1, 2, 3).map(Person("Vlad", _))))
val listIntVar = Var(List(1, 2, 3))

val component = div(
  child <-- listPersonVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(listPersonVar),
  child <-- listIntVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  Form.renderVar(listIntVar)
)
