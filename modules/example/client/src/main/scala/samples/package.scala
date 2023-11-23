package samples

import dev.cheleb.scalamigen.Defaultable
import dev.cheleb.scalamigen.WidgetFactory
import com.raquo.laminar.api.L.{button as lbutton, ul as lul, *}
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.TitleLevel
import be.doeraene.webcomponents.ui5.configkeys.InputType
import com.raquo.laminar.modifiers.EventListener
import be.doeraene.webcomponents.ui5.configkeys.ListSeparator

case class Cat(name: String, weight: Int)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)

val laminar = new WidgetFactory:
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
val ui5 = new WidgetFactory:
  def text: HtmlElement = Input(
    _.showClearIcon := true
  )
  def numeric: HtmlElement = Input(
    _.tpe := InputType.Number,
    _.showClearIcon := true
  )
  def button: HtmlElement = Button()
  def link(text: String, el: EventListener[_, _]): HtmlElement = Link(text, el)
  def panel(headerText: String): HtmlElement = Panel(
    _.headerText := headerText,
    _.headerLevel := TitleLevel.H3
  )
  def ul(id: String): HtmlElement = UList(
    _.id := id,
    width := "100%",
    _.noDataText := "No  data",
    _.separators := ListSeparator.None
  )

given WidgetFactory = laminar
