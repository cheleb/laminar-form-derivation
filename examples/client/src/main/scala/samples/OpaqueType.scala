package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val opaque: Sample = {

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
    |opaque type Password = String
    |object Password:
    |  def apply(password: String): Password = password
    |  given Form[Password] = secretForm(apply)
    |
    |// In another file...
    |
    |case class Person(
    |      name: String,
    |      password: Password
    |  )
    |
    |val simpleVar = Var(Person("Vlad",  Password("123456"))
    |
    |simpleVar.asForm
    """.stripMargin
  )
}
