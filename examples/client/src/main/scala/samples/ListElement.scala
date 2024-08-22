package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

case class Person2(id: Int, name: String, age: Int)

case class ListElement(
    ints: List[Person2]
)

val listPersonVar = Var(
  ListElement(List(1, 2, 3).map(id => Person2(id, "Vlad", 20)))
)
val listIntVar = Var(List(1, 2, 3))

given (Person2 => Int) = _.id

val list = Sample(
  "List",
  div(
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
    }
    // Form.renderVar(listIntVar)
  )
)
