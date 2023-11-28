package dev.cheleb.scalamigen

import io.github.iltotore.iron.IronType

/** Type validator for
  * [IronType](https://iltotore.github.io/iron/docs/index.html).
  *
  * Iron is a library for compile-time or runtime type validation.
  */
trait IronTypeValidator[T, C] {

  /** Validate a string against an IronType.
    */
  def validate(a: String): Either[String, IronType[T, C]]
}
