package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val opaque = {

  case class Person(
      name: String,
      password: Password
  )

  val simpleVar = Var(Person("Vlad", Password("123456")))
  Sample(
    "Opaque Type",
    simpleVar.asForm,
    div(
      child <-- simpleVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """
    |case class Person(
    |                name: String,
    |                weight: Int,
    |                hairsCount: BigInt :| GreaterEqual[100000],
    |                kind: Boolean = true)
    |
    |val simpleVar = Var(Person("Vlad", 66, BigInt(100000).refineUnsafe))
    |
    |simpleVar.asForm
    """.stripMargin
  )
}
