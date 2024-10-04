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

given WidgetFactory = if true then UI5WidgetFactory else LaminarWidgetFactory
