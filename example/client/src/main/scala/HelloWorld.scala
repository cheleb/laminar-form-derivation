package demo

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.Form.given
import org.scalajs.dom
import com.raquo.laminar.api.L.*
import magnolia1.*
import be.doeraene.webcomponents.ui5.SideNavigation
import be.doeraene.webcomponents.ui5.Icon

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
      SideNavigation(
        _.events.onSelectionChange
          .map(_.detail.item.dataset.get("sample")) --> Observer[
          Option[String]
        ] {

          case None        =>
          case Some(value) => println(value)

        },
        Seq(
          SideNavigation.item(
            _.text := "Item 1",
            dataAttr("sample") := "item1"
          ),
          SideNavigation.item(
            _.text := "Item 2",
            dataAttr("sample") := "item2"
          ),
          SideNavigation.item(
            _.text := "Item 3",
            dataAttr("sample") := "item3"
          )
        )
      ),
      div(
        child <-- itemVar.signal.map { item =>
          div(
            s"$item"
          )
        },
        Form.renderVar(itemVar)
      )
    )

  val containerNode = dom.document.getElementById("root")
  render(containerNode, myApp)
}
