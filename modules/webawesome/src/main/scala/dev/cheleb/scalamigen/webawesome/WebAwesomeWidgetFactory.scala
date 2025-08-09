package dev.cheleb.scalamigen.webawesome

import com.raquo.laminar.api.L.*
import io.github.nguyenyou.webawesome.laminar.*

import com.raquo.laminar.modifiers.EventListener

import dev.cheleb.scalamigen.WidgetFactory
import com.raquo.laminar.api.L

//import io.github.nguyenyou.ui5.webcomponents.ui5Webcomponents.distTypesTitleLevelMod.TitleLevel

/** UI5WidgetFactory is a factory for [SAP UI5
  * widgets](https://sap.github.io/ui5-webcomponents/).
  *
  * It relies on [Laminar UI5
  * bindings](https://github.com/sherpal/LaminarSAPUI5Bindings).
  */
object WebAwesomeWidgetFactory extends WidgetFactory:

  override def renderCheckbox: L.HtmlElement = Checkbox()()

  override def renderDatePicker: L.HtmlElement = Input(
    _.tpe := "date"
  )()

  override def renderDialog(
      title: String,
      content: HtmlElement,
      openDialogBus: EventBus[Boolean]
  ): HtmlElement =
    Dialog(
      _.label := title
      // _.onClose --> { _ => }
    )().amend(content)

  override def renderSecret: L.HtmlElement = Input(
    _.tpe := "password"
  )()

  override def renderText: HtmlElement = Input(
    //  _.showClearIcon := true,
    _.placeholder := "Enter text"
  )()
  override def renderLabel(required: Boolean, name: String): HtmlElement =
    span(name)

  override def renderNumeric: HtmlElement = Input(
    _.tpe := "number",
    _.placeholder := "Enter number"
  )()
  override def renderButton: HtmlElement = Button()()
  override def renderLink(text: String, el: EventListener[?, ?]): HtmlElement =
    a(
      text,
      href := "#",
      el
    )
  override def renderUL(id: String): HtmlElement = ul(idAttr := id)

  override def renderPanel(headerText: Option[String]): HtmlElement =
    headerText match
      case None             => div()
      case Some(headerText) =>
        div(
          headerText
        )

  override def renderSelect(selectedIndex: Int)(
      f: Int => Unit
  ): HtmlElement =
    Select(
      _.onChange.mapToValue.map(_.toInt) --> { ds =>
        f(ds)
      },
      _.value := s"$selectedIndex"
    )()

  override def renderOption(
      label: String,
      idx: Int,
      isSelected: Boolean
  ): HtmlElement =
    UOption(
      _.label := label,
      _.value := s"$idx",
      _.selected := isSelected
    )(label)
