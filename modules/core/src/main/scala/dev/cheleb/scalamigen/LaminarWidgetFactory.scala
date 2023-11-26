package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import com.raquo.laminar.modifiers.EventListener
import org.scalajs.dom.HTMLSelectElement

/** This is raw laminar implementation of the widget factory.
  */
object LaminarWidgetFactory extends WidgetFactory:
  def renderText: HtmlElement = input(
    tpe := "text"
  )
  def renderLabel(required: Boolean, name: String): HtmlElement = span(
    name
  )
  def renderNumeric: HtmlElement = input(
    tpe := "number"
  )
  def renderButton: HtmlElement = button()
  def renderLink(text: String, el: EventListener[_, _]): HtmlElement = a(
    text,
    href := "#",
    el
  )
  def renderUL(id: String): HtmlElement = ul(idAttr := id)
  def renderPanel(headerText: String): HtmlElement = div(headerText)

  def renderSelect(f: Int => Unit): HtmlElement = select(
    onChange.map(
      _.target.asInstanceOf[HTMLSelectElement].selectedIndex
    ) --> { ds =>
      f(ds)
    }
  )

  def renderOption(label: String, idx: Int, isSelected: Boolean): HtmlElement =
    option(
      label,
      value := s"$idx",
      selected := isSelected
    )
