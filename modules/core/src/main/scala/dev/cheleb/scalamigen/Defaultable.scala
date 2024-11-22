package dev.cheleb.scalamigen

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

/** Typeclass for default values.
  *
  * This typeclass is used to provide default values for a given type. It is
  * used to provide default values for form fields when creating a new object,
  * for example.
  *
  * It is necessary to provide a default value for every type used in the
  * application and wrapped in a option.
  */
trait Defaultable[A] {

  /** The default value for the type.
    */
  def default: A

  /** The label for the type.
    */
  def label: String =
    NameUtils.titleCase(default.getClass.getSimpleName)
}

object Defaultable {

  given Defaultable[Boolean] with
    def default = false

  /** Default value for Int is 0.
    */
  given Defaultable[Int] with
    def default = 0

  given Defaultable[Double] with
    def default = 0

  given Defaultable[Float] with
    def default = 0

  given Defaultable[BigDecimal] with
    def default = 0
  given Defaultable[BigInt] with
    def default = 0

  /** Default value for String is "".
    */
  given Defaultable[String] with
    def default = ""

    /** Default value for [Iron type Double
      * positive](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint/numeric$.html#Positive-0)
      * is 0.0.
      */
  given Defaultable[IronType[Double, Positive]] with
    def default = 1.0.refineUnsafe[Positive]

}
