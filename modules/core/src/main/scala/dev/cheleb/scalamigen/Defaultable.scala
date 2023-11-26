package dev.cheleb.scalamigen

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
  def label: String = default.getClass.getSimpleName()
}
