package samples

import dev.cheleb.scalamigen.Defaultable
import dev.cheleb.scalamigen.WidgetFactory
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.Panel

case class Cat(name: String, weight: Int)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)

val laminarZ = new WidgetFactory:
  def numericForm: HtmlElement = input(
    tpe := "number"
  )
  def objectForm: HtmlElement = div()
val ui5 = new WidgetFactory:
  def numericForm: HtmlElement = input(
    tpe := "number"
  )
  def objectForm: HtmlElement = Panel()

given WidgetFactory = laminarZ
