package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val list = {
  case class Person2(id: Int, name: String, age: Int)

  case class ListElement(
      ints: List[Person2]
  )

  given (Person2 => Int) = _.id

  val listPersonVar = Var(
    ListElement(List(1, 2, 3).map(id => Person2(id, "Vlad", 20)))
  )

  Sample(
    "List",
    listPersonVar.asForm,
    div(child <-- listPersonVar.signal.map { item =>
      div(
        s"$item"
      )
    }),
    """|case class Person2(id: Int, name: String, age: Int)
         |
         |case class ListElement(
         |     ints: List[Person2]
         |)
         |""".stripMargin
  )

}
