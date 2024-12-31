package dev.cheleb.scalamigen

import scala.util.Try

/** A trait representing a validator for a type A.
  *
  * A validator is a function that takes a string and returns either an error
  * message or a value of type A.
  */
trait Validator[A] {
  def validate(str: String): Either[String, A]
}

/** Validators for common types. They are the base validation used by Iron
  * derivations.
  */
object Validator {

  /** A validator for strings.
    */
  given Validator[String] with
    def validate(str: String): Either[String, String] =
      Right(str)

  /** A validator for doubles.
    */
  given Validator[Double] with
    def validate(str: String): Either[String, Double] =
      str.toDoubleOption.toRight("Not a double")

  /** A validator for integers.
    */
  given Validator[Int] with
    def validate(str: String): Either[String, Int] =
      str.toIntOption.toRight("Not a int")

  /** A validator for longs.
    */
  given Validator[Float] with
    def validate(str: String): Either[String, Float] =
      str.toFloatOption.toRight("Not a float")

  /** A validator for big integers.
    */
  given Validator[BigInt] with
    def validate(str: String): Either[String, BigInt] =
      Try(BigInt.apply(str)).toEither.left.map(_.getMessage)

  /** A validator for big decimals.
    */
  given Validator[BigDecimal] with
    def validate(str: String): Either[String, BigDecimal] =
      Try(BigDecimal.apply(str)).toEither.left.map(_.getMessage)

}
