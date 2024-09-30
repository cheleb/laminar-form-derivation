package dev.cheleb.scalamigen

trait ValidationEvent

case object ValidEvent extends ValidationEvent

final case class InvalideEvent(errorMessage: String) extends ValidationEvent

case object HiddenEvent extends ValidationEvent

case object ShownEvent extends ValidationEvent

enum ValidationStatus:
  case Unknown
  case Valid
  case Invalid(message: String, visible: Boolean)
  case Hidden(first: Boolean)
  case Shown
