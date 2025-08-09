package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.HtmlElement
import com.raquo.laminar.modifiers.EventListener

import com.raquo.airstream.eventbus.EventBus

/** This is a trait that defines the interface for the widget factory.
  */
trait WidgetFactory:

  /** Render a checkbox.
    */
  def renderCheckbox: HtmlElement

  /** Render a date picker.
    */
  def renderDatePicker: HtmlElement

  def renderDialog(
      title: String,
      content: HtmlElement,
      openDialogBus: EventBus[Boolean]
  ): HtmlElement

  /** Render a text input, for strings.
    */
  def renderText: HtmlElement

  /** Render a password input, for secret strings.
    */
  def renderSecret: HtmlElement

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
  def renderPanel(headerText: Option[String]): HtmlElement

  /** Render an unordered list. This is a container for other widgets derived
    * from a case class.
    */
  def renderUL(id: String): HtmlElement

  /** Render a select.
    * @param selectedIndex
    *   the index of the initially selected option
    * @param f
    *   a callback function that is called when the selected option changes,
    *   this function receives the new index as a parameter and should update
    *   the model accordingly.
    */
  def renderSelect(selectedIndex: Int)(f: Int => Unit): HtmlElement

  /** Render an option.
    * @param label
    *   the label of the option
    * @param idx
    *   the index of the option
    * @param selected
    *   whether the option is selected
    */
  def renderOption(label: String, idx: Int, selected: Boolean): HtmlElement
