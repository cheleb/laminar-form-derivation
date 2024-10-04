package dev.cheleb.scalamigen

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

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

  /** Validator for [Iron type Double
    * positive](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint/numeric$.html#Positive-0).
    */
  given IronTypeValidator[Double, Positive] with
    def validate(a: String): Either[String, IronType[Double, Positive]] =
      a.toDoubleOption match
        case None         => Left("Not a number")
        case Some(double) => double.refineEither[Positive]

  // inline transparent given [A, C](using
  //     baseValidator: Validator[A],
  //     constraint: Constraint[A, C]
  // ): IronTypeValidator[A, C] = new IronTypeValidator[A, C] {
  //   def validate(a: String): Either[String, IronType[A, C]] =
  //     baseValidator.validate(a).flatMap { a =>
  //       a.refineEither[C]
  //     }
  // }
}
