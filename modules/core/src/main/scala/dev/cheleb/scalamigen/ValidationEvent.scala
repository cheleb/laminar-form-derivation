package dev.cheleb.scalamigen

/** A sealed trait representing the possible events that can be emitted by a
  * validation.
  *
  * Events are emitted by the validation of a field. They are used to update the
  * state.
  */
trait ValidationEvent

/** The event emitted when the validation is successful.
  *
  * It will clear any error message for a field.
  */
case object ValidEvent extends ValidationEvent

/** The event emitted when the validation is unsuccessful.
  *
  * @param errorMessage
  *   The error message.
  */
final case class InvalideEvent(errorMessage: String) extends ValidationEvent

/** The event emitted when the field is hidden (when Option is set to None).
  *
  * It will then ignore any validation status.
  */
case object HiddenEvent extends ValidationEvent

/** The event emitted when the field is shown (when Option is set to Some).
  *
  * It will then redem any validation status.
  */
case object ShownEvent extends ValidationEvent

/** Validation status.
  *
  * It is used to determine the status of a field.
  */
enum ValidationStatus:
  case Unknown
  case Valid
  case Invalid(message: String, visible: Boolean)
  case Hidden(first: Boolean)
  case Shown
