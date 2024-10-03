package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val simple = {
  val simpleVar = Var(Cat("Scala le chat", 6))
  Sample(
    "Simple",
    simpleVar.asForm,
    div(
      child <-- simpleVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """
    |case class Cat(name: String, weight: Int, kind: Boolean = true)
    |
    |val simpleVar = Var(Cat("Scala le chat", 6))
    |
    |simpleVar.asForm
    """.stripMargin
  )
}
