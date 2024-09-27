package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

val list = {
  case class Person2(id: Int, name: String, age: Int)

  case class ListElement(
      ints: List[Person2]
  )

  val listPersonVar = Var(
    ListElement(List(1, 2, 3).map(id => Person2(id, "Vlad", 20)))
  )

  given (Person2 => Int) = _.id
  Sample(
    "List",
    div(
      child <-- listPersonVar.signal.map { item =>
        div(
          s"$item"
        )
      },
      listPersonVar.asForm
    )
  )
}
