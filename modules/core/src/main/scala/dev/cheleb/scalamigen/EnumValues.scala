package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

/** Annotation for enum values
  *
  * Use this annotation to specify enum values for sealed traits.
  */
class EnumValues[A](val values: Array[A]) extends StaticAnnotation

/** Annotation for field names
  *
  * Use this annotation to specify field names for case class fields.
  */
class FieldName(val value: String) extends StaticAnnotation

class PanelName(val value: String) extends StaticAnnotation
class NoPanel extends StaticAnnotation
