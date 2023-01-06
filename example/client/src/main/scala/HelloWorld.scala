package demo

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.Form.given
import org.scalajs.dom
import com.raquo.laminar.api.L.*
import magnolia1.*

// Define some models
case class Person(
    name: String,
    fav: Pet,
    pet: Option[Pet],
    email: Option[String],
    age: Int
)
case class Pet(name: String, age: Int, House: House, size: Option[Int])

case class House(capacity: Int)

// Provide default for optional
given Defaultable[Pet] with
  def default = Pet("No pet", 0, House(0), None)

// Instance your model
val agnes =
  Person(
    "Vlad",
    Pet("Batman", 666, House(2), Some(169)),
    Some(Pet("Wolfy", 12, House(1), Some(42))),
    Some("vlad.dracul@gmail.com"),
    48
  )

val itemVar = Var(agnes)

object App extends App {

  val myApp =
    div(
      child <-- itemVar.signal.map { item =>
        div(
          s"$item"
        )
      },
      Form.renderVar(itemVar)
    )

  val containerNode = dom.document.getElementById("root")
  render(containerNode, myApp)
}
