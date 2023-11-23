package dev.cheleb.scalamigen.ui5

import com.raquo.laminar.api.L.{button as lbutton, ul as lul, *}
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.InputType
import com.raquo.laminar.modifiers.EventListener
import be.doeraene.webcomponents.ui5.configkeys.ListSeparator
import be.doeraene.webcomponents.ui5.configkeys.TitleLevel

object UI5WidgetFactory extends dev.cheleb.scalamigen.WidgetFactory:
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
