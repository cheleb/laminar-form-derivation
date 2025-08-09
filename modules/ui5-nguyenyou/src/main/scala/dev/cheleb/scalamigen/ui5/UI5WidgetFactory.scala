package dev.cheleb.scalamigen.ui5nguyenyou

import com.raquo.laminar.api.L.*
import io.github.nguyenyou.ui5.webcomponents.laminar.*

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
object UI5WidgetFactory extends WidgetFactory:

  override def renderCheckbox: L.HtmlElement = CheckBox()()

  override def renderDatePicker: L.HtmlElement = DatePicker(
    _.formatPattern := "yyyy-MM-dd"
  )()

  override def renderDialog(
      title: String,
      content: HtmlElement,
      openDialogBus: EventBus[Boolean]
  ): HtmlElement =
    Dialog(
      _.headerText := title,
      _.onClose --> { _ => }
    )().amend(content)

  override def renderSecret: L.HtmlElement = Input(
    _.tpe := "Password"
  )()

  override def renderText: HtmlElement = Input(
    _.showClearIcon := true,
    _.placeholder := "Enter text"
  )()
  override def renderLabel(required: Boolean, name: String): HtmlElement =
    Label(
      _.required := required,
      _.showColon := false
//    _.text := name
    )(name)

  override def renderNumeric: HtmlElement = Input(
    _.tpe := "Number",
    _.placeholder := "Enter number"
  )()
  override def renderButton: HtmlElement = Button()()
  override def renderLink(text: String, el: EventListener[?, ?]): HtmlElement =
    Link()(text, el)
  override def renderUL(id: String): HtmlElement = ListItemGroup(
    _.id := id
  )()
  override def renderPanel(headerText: Option[String]): HtmlElement =
    headerText match
      case Some(headerText) =>
        Panel(
          _.headerText := headerText,
          _.headerLevel := "H3"
        )()
      case None =>
        div(cls := "srf-table")

  override def renderSelect(
      selectedIndex: Int
  )(f: Int => Unit): HtmlElement = Select(
    _.onChange
      .map(_.detail.selectedOption.value) --> { ds =>
      ds.foreach { case idx: String =>
        f(idx.toInt)
      }

    }
  )()

  override def renderOption(
      label: String,
      idx: Int,
      selected: Boolean
  ): HtmlElement =
    Opt(
      _.value := s"$idx",
      _.selected := selected
    )(
      label
    )
