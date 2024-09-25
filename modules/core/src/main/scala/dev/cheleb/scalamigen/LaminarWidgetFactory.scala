package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import com.raquo.laminar.modifiers.EventListener
import org.scalajs.dom.HTMLSelectElement
import com.raquo.laminar.api.L

/** This is raw laminar implementation of the widget factory.
  */
object LaminarWidgetFactory extends WidgetFactory:

  override def renderCheckbox: L.HtmlElement = input(
    tpe := "checkbox"
  )

  override def renderDatePicker: HtmlElement = input(
    tpe := "date"
  )

  override def renderSecret: HtmlElement = input(
    tpe := "password"
  )

  override def renderText: HtmlElement = input(
    tpe := "text"
  )
  override def renderLabel(required: Boolean, name: String): HtmlElement = span(
    name
  )
  override def renderNumeric: HtmlElement = input(
    tpe := "number"
  )
  override def renderButton: HtmlElement = button()
  override def renderLink(text: String, el: EventListener[?, ?]): HtmlElement =
    a(
      text,
      href := "#",
      el
    )
  override def renderUL(id: String): HtmlElement = ul(idAttr := id)
  override def renderPanel(headerText: Option[String]): HtmlElement =
    headerText match
      case None => div()
      case Some(headerText) =>
        div(
          headerText
        )

  override def renderSelect(f: Int => Unit): HtmlElement = select(
    onChange.map(
      _.target.asInstanceOf[HTMLSelectElement].selectedIndex
    ) --> { ds =>
      f(ds)
    }
  )

  override def renderOption(
      label: String,
      idx: Int,
      isSelected: Boolean
  ): HtmlElement =
    option(
      label,
      value := s"$idx",
      selected := isSelected
    )
