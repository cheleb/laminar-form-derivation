package dev.cheleb.scalamigen.ui5

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*

import com.raquo.laminar.modifiers.EventListener
import be.doeraene.webcomponents.ui5.configkeys.ListSeparator
import be.doeraene.webcomponents.ui5.configkeys.TitleLevel

import dev.cheleb.scalamigen.WidgetFactory
import com.raquo.laminar.api.L
import be.doeraene.webcomponents.ui5.configkeys.InputType.Password

/** UI5WidgetFactory is a factory for [SAP UI5
  * widgets](https://sap.github.io/ui5-webcomponents/).
  *
  * It relies on [Laminar UI5
  * bindings](https://github.com/sherpal/LaminarSAPUI5Bindings).
  */
object UI5WidgetFactory extends WidgetFactory:

  override def renderSecret: L.HtmlElement = Input(
    _.tpe := Password
  )

  def renderText: HtmlElement = Input(
    _.showClearIcon := true
  )
  def renderLabel(required: Boolean, name: String): HtmlElement = Label(
    _.required := required,
    _.showColon := false
//    _.text := name
  ).amend(name)

  def renderNumeric: HtmlElement = Input(
    tpe := "number"
  )
  def renderButton: HtmlElement = Button()
  def renderLink(text: String, el: EventListener[?, ?]): HtmlElement =
    Link(text, el)
  def renderUL(id: String): HtmlElement = UList(
    _.id := id,
    width := "100%",
    _.noDataText := "No  data",
    _.separators := ListSeparator.None
  )
  def renderPanel(headerText: String): HtmlElement = Panel(
    _.headerText := headerText,
    _.headerLevel := TitleLevel.H3
  )

  def renderSelect(f: Int => Unit): HtmlElement = Select(
    _.events.onChange
      .map(_.detail.selectedOption.dataset) --> { ds =>
      ds.get("idx").foreach(idx => f(idx.toInt))

    }
  )

  def renderOption(
      label: String,
      idx: Int,
      selected: Boolean
  ): HtmlElement =
    Select.option(
      label,
      dataAttr("idx") := s"$idx",
      _.selected := selected
    )
