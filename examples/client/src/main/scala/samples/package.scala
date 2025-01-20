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

opaque type ExtraString = String
object ExtraString:
    def apply(s: String): ExtraString = s
    // given Form[ExtraString] = stringForm(identity)
    given Form[ExtraString] = stringFormWithValidation(using 
        new Validator[ExtraString] {
            override def validate(str: String): Either[String, ExtraString] = 
                str.matches("^[a-fA-F0-9]+$") match
                    case true => Right(str)
                    case false => Left("expected hexadecimal string (just for demo)")
        }
    )
    given Defaultable[ExtraString] with
        def default: ExtraString = ""


opaque type ExtraInt = Int
object ExtraInt:
    def apply(i: Int): ExtraInt = i
    given Form[ExtraInt] = numericForm(_.toIntOption, 0)
    given Defaultable[ExtraInt] with
        def default: ExtraInt = 0

given WidgetFactory = if true then UI5WidgetFactory else LaminarWidgetFactory
