package samples

import dev.cheleb.scalamigen.*
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory

opaque type CurrencyCode = String

object CurrencyCode:
  def apply(code: String): CurrencyCode = code

opaque type Password = String
object Password:
  def apply(password: String): Password = password
  given Form[Password] = secretForm(apply)

case class Cat(name: String, weight: Int, kind: Boolean = true)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)

given WidgetFactory = if true then UI5WidgetFactory else LaminarWidgetFactory
