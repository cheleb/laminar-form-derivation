package dev.cheleb.scalamigen

trait ValidationEvent

case object ValidEvent extends ValidationEvent

final case class InvalideEvent(errorMessage: String) extends ValidationEvent

case object HiddenEvent extends ValidationEvent

case object ShownEvent extends ValidationEvent

case class ValidationStatus(errorMessage: String, shown: Boolean) {
  def this(errorMessage: String) = this(errorMessage, true)
}
