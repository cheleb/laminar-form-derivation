package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.{button as lbutton, ul as lul, *}
import com.raquo.laminar.modifiers.EventListener

object LaminarWidgetFactory extends WidgetFactory:
  def text: HtmlElement = input(
    tpe := "text"
  )
  def numeric: HtmlElement = input(
    tpe := "number"
  )
  def button: HtmlElement = lbutton()
  def link(text: String, el: EventListener[_, _]): HtmlElement = a(
    text,
    href := "#",
    el
  )
  def ul(id: String): HtmlElement = lul(idAttr := id)
  def panel(headerText: String): HtmlElement = div(headerText)
