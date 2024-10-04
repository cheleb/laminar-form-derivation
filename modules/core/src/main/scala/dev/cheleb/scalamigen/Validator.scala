package dev.cheleb.scalamigen

trait Validator[A] {
  def validate(str: String): Either[String, A]
}

object Validator {
  given Validator[Double] = new Validator[Double] {
    override def validate(str: String): Either[String, Double] =
      str.toDoubleOption.toRight("Not a number")
  }
}
