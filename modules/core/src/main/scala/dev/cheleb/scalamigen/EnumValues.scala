package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

/** Annotation for enum values
  *
  * Use this annotation to specify enum values for sealed traits.
  */
class EnumValues[A](val values: Array[A]) extends StaticAnnotation
