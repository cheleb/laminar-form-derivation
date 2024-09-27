package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

/** Annotation for field names
  *
  * Use this annotation to specify field names for case class fields.
  */
class FieldName(val value: String) extends StaticAnnotation

class Panel(val name: String) extends StaticAnnotation
class NoPanel extends StaticAnnotation
