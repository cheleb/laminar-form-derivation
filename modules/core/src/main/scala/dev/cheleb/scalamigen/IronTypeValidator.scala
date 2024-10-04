package dev.cheleb.scalamigen

import io.github.iltotore.iron.*

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

object IronTypeValidator {

  /** Create an IronTypeValidator for a given IronType.
    *
    * @param baseValidator
    * @param constraint
    * @return
    */
  given [A, C](using
      baseValidator: Validator[A],
      constraint: RuntimeConstraint[A, C]
  ): IronTypeValidator[A, C] with
    def validate(a: String): Either[String, IronType[A, C]] =
      baseValidator.validate(a).flatMap { a =>
        if constraint.test(a) then Right(a.assume[C])
        else Left(constraint.message)
      }

}
