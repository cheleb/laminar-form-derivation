package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.HtmlElement
import com.raquo.laminar.modifiers.EventListener

/** This is a trait that defines the interface for the widget factory.
  */
trait WidgetFactory:
  /** Render a text input, for strings.
    */
  def renderText: HtmlElement

  /** Render a label for a widget.
    */
  def renderLabel(required: Boolean, name: String): HtmlElement

  /** Render a numeric input, for numbers.
    */
  def renderNumeric: HtmlElement

  /** Render a button.
    */
  def renderButton: HtmlElement

  /** Render a link.
    */
  def renderLink(text: String, obs: EventListener[?, ?]): HtmlElement

  /** Render a panel. This is a container for other widgets derived from a case
    * class.
    */
  def renderPanel(headerText: String): HtmlElement

  /** Render an unordered list. This is a container for other widgets derived
    * from a case class.
    */
  def renderUL(id: String): HtmlElement

  /** Render a select.
    */
  def renderSelect(f: Int => Unit): HtmlElement

  /** Render an option.
    */
  def renderOption(label: String, idx: Int, selected: Boolean): HtmlElement
