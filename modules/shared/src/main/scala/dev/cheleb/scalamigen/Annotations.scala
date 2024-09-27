package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

/** Annotation for field names
  *
  * Use this annotation to specify field names for case class fields.
  */
class FieldName(val value: String) extends StaticAnnotation

/** @param name
  */
class Panel(val name: String, val asTable: Boolean = true)
    extends StaticAnnotation

/** */
class NoPanel(val asTable: Boolean = true) extends StaticAnnotation
