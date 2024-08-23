package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*

import scala.util.Try
import com.raquo.airstream.state.Var
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.HTMLElement
import com.raquo.laminar.nodes.ReactiveHtmlElement

extension [A](v: Var[A])
  def asForm(using WidgetFactory, Form[A]) = Form.renderVar(v)

trait Form[A] { self =>

  def isAnyRef = false

  def fromString(s: String): Option[A] = None
  def fromString(s: String, variable: Var[A], errorVar: Var[String]): Unit = ()

  def toString(a: A) = a.toString

  def render(
      variable: Var[A]
  )(using factory: WidgetFactory): HtmlElement =
    render(variable, () => ())

  def render(
      variable: Var[A],
      syncParent: () => Unit,
      values: List[A] = List.empty
  )(using factory: WidgetFactory): HtmlElement

  given Owner = unsafeWindowOwner

  def labelled(name: String, required: Boolean): Form[A] = new Form[A] {
    override def render(
        variable: Var[A],
        syncParent: () => Unit,
        values: List[A] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      div(
        div(
          factory.renderLabel(required, name)
        ),
        div(
          self.render(variable, syncParent, values)
        )
      )

  }
  def xmap[B](to: (B, A) => B)(from: B => A): Form[B] = new Form[B] {
    override def render(
        variable: Var[B],
        syncParent: () => Unit,
        values: List[B] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      self.render(variable.zoom(from)(to), syncParent, values.map(from))
  }

}

object Form extends AutoDerivation[Form] {

  def renderVar[A](v: Var[A], syncParent: () => Unit = () => ())(using
      WidgetFactory
  )(using
      fa: Form[A]
  ): ReactiveHtmlElement[HTMLElement] =
    fa.render(v, syncParent)

  def join[A](
      caseClass: CaseClass[Typeclass, A]
  ): Form[A] = new Form[A] {

    override def isAnyRef: Boolean = true
    override def render(
        variable: Var[A],
        syncParent: () => Unit = () => (),
        values: List[A] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      factory
        .renderPanel(caseClass.typeInfo.short)
        .amend(
          className := "panel panel-default",
          caseClass.params.map { param =>
            val isOption = param.deref(variable.now()).isInstanceOf[Option[?]]

            val enumValues =
              if param.annotations.isEmpty then List.empty[A]
              else if param.annotations(0).isInstanceOf[EnumValues[?]] then
                param.annotations(0).asInstanceOf[EnumValues[A]].values.toList
              else List.empty[A]

            param.typeclass
              .labelled(param.label, !isOption)
              .render(
                variable.zoom { a =>
                  Try(param.deref(a))
                    .getOrElse(param.default)
                    .asInstanceOf[param.PType]
                }((_, value) =>
                  caseClass.construct { p =>
                    if (p.label == param.label) value
                    else p.deref(variable.now())
                  }
                )(unsafeWindowOwner),
                syncParent,
                enumValues.map(_.asInstanceOf[param.PType])
              )
              .amend(
                idAttr := param.label
              )
          }.toSeq
        )
  }

  /** Split a sealed trait into a form
    *
    * @param sealedTrait
    * @return
    */
  def split[A](sealedTrait: SealedTrait[Form, A]): Form[A] = new Form[A] {

    override def isAnyRef: Boolean = true
    override def render(
        variable: Var[A],
        syncParent: () => Unit,
        values: List[A] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      if sealedTrait.isEnum then
        if values.isEmpty
        then // No enum values provided, than render as constant
          div(variable.now().toString())
        else
          val valuesLabels = values.map(_.toString)
          div(
            factory
              .renderSelect(idx => variable.set(values(idx)))
              .amend(
                valuesLabels.map { label =>
                  factory.renderOption(
                    label,
                    values
                      .map(_.toString)
                      .indexOf(label),
                    label == variable.now().toString
                  )
                }.toSeq
              )
          )
      else div("Not an enum.")

  }

}

/** Use this form to render a string that can be converted to A, can be used for
  * Opaque types.
  */
def stringForm[A](to: String => A) = new Form[A]:
  override def render(
      variable: Var[A],
      syncParent: () => Unit,
      values: List[A] = List.empty
  )(using factory: WidgetFactory): HtmlElement =
    factory.renderText.amend(
      value <-- variable.signal.map(_.toString),
      onInput.mapToValue.map(to) --> { v =>
        variable.set(v)
        syncParent()
      }
    )

/** Form for a numeric type.
  */
def numericForm[A](f: String => Option[A], zero: A): Form[A] = new Form[A] {
  self =>
  override def fromString(s: String): Option[A] =
    f(s).orElse(Some(zero))
  override def render(
      variable: Var[A],
      syncParent: () => Unit,
      values: List[A] = List.empty
  )(using factory: WidgetFactory): HtmlElement =
    factory.renderNumeric
      .amend(
        value <-- variable.signal.map { str =>
          str.toString()
        },
        onInput.mapToValue --> { v =>
          fromString(v).foreach(variable.set)
          syncParent()
        }
      )
}
