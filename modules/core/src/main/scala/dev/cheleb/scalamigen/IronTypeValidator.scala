package dev.cheleb.scalamigen

import io.github.iltotore.iron.IronType

/** Type validator for IronType.
  *
  * Iron is a library for compile-time type validation. See
  * https://iltotore.github.io/iron/ for more information.
  */
trait IronTypeValidator[T, C] {

  /** Validate a string against an IronType.
    */
  def validate(a: String): Either[String, IronType[T, C]]
}
