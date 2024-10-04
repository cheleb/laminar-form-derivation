package dev.cheleb.scalamigen

trait Validator[A] {
  def validate(str: String): Either[String, A]
}

object Validator {
  given Validator[Double] with
    def validate(str: String): Either[String, Double] =
      str.toDoubleOption.toRight("Not a number")
  given Validator[Int] with
    def validate(str: String): Either[String, Int] =
      str.toIntOption.toRight("Not a number")
}
