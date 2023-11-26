package dev.cheleb.scalamigen.ui5

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.InputType
import com.raquo.laminar.modifiers.EventListener
import be.doeraene.webcomponents.ui5.configkeys.ListSeparator
import be.doeraene.webcomponents.ui5.configkeys.TitleLevel

object UI5WidgetFactory extends dev.cheleb.scalamigen.WidgetFactory:
  def renderText: HtmlElement = Input(
    _.showClearIcon := true
  )

  def renderLabel(required: Boolean, name: String): HtmlElement = Label(
    _.required := required,
    _.showColon := false
//    _.text := name
  ).amend(name)

  def renderNumeric: HtmlElement = Input(
    _.tpe := InputType.Number,
    _.showClearIcon := true
  )
  def renderButton: HtmlElement = Button()
  def renderLink(text: String, el: EventListener[_, _]): HtmlElement =
    Link(text, el)
  def renderPanel(headerText: String): HtmlElement = Panel(
    _.headerText := headerText,
    _.headerLevel := TitleLevel.H3
  )
  def renderUL(id: String): HtmlElement = UList(
    _.id := id,
    width := "100%",
    _.noDataText := "No  data",
    _.separators := ListSeparator.None
  )

  override def renderSelect(f: Int => Unit): HtmlElement = Select(
    _.events.onChange
      .map(_.detail.selectedOption.dataset) --> { ds =>
      ds.get("idx").foreach(idx => f(idx.toInt))

    }
  )

  override def renderOption(
      label: String,
      idx: Int,
      selected: Boolean
  ): HtmlElement =
    Select.option(
      label,
      dataAttr("idx") := s"$idx",
      _.selected := selected
    )
