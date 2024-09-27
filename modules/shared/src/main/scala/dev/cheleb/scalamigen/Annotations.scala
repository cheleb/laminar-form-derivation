package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

/** Annotation for field names
  *
  * Use this annotation to specify field names for case class fields.
  */
class FieldName(val value: String) extends StaticAnnotation

/** @param name
  */
case class Panel(
    name: String,
    asTable: Boolean = true,
    fieldCss: String = "srf-field",
    labelCss: String = "srf-label",
    panelCss: String = "srf-panel"
) extends StaticAnnotation

/** */
case class NoPanel(
    asTable: Boolean = true,
    fieldCss: String = "srf-field",
    labelCss: String = "srf-label",
    panelCss: String = "srf-panel"
) extends StaticAnnotation
