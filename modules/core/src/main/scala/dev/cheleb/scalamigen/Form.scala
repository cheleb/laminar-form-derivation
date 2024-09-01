package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*

import scala.util.Try
import com.raquo.airstream.state.Var
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.HTMLElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import magnolia1.SealedTrait.Subtype

extension [A](v: Var[A])
  def asForm(using WidgetFactory, Form[A]) = Form.renderVar(v)

trait Form[A] { self =>

  def isAnyRef = false

  def fromString(s: String): Option[A] = None

  def fromString(s: String, variable: Var[A], errorVar: Var[String]): Unit = ()

  def toString(a: A) = a.toString

  def render(
      variable: Var[A],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement

  given Owner = unsafeWindowOwner

  def labelled(name: String, required: Boolean): Form[A] = new Form[A] {
    override def render(
        variable: Var[A],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      div(
        div(
          factory.renderLabel(required, name)
        ),
        div(
          self.render(variable, syncParent)
        )
      )

  }
  def xmap[B](to: (B, A) => B)(from: B => A): Form[B] = new Form[B] {
    override def render(
        variable: Var[B],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      self.render(variable.zoom(from)(to), syncParent)
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
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement = {

      val panel =
        caseClass.annotations.find(_.isInstanceOf[PanelName]) match
          case None =>
            caseClass.annotations.find(_.isInstanceOf[NoPanel]) match
              case None =>
                Some(caseClass.typeInfo.short)
              case Some(_) =>
                None
          case Some(value) =>
            Option(value.asInstanceOf[PanelName].value)

      factory
        .renderPanel(panel)
        .amend(
          className := "panel panel-default",
          caseClass.params.map { param =>
            val isOption = param.deref(variable.now()).isInstanceOf[Option[?]]

            val fieldName = param.annotations
              .find(_.isInstanceOf[FieldName]) match
              case None => param.label
              case Some(value) =>
                value.asInstanceOf[FieldName].value

            param.typeclass
              .labelled(fieldName, !isOption)
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
                syncParent
              )
              .amend(
                idAttr := param.label
              )
          }.toSeq
        )
    }
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
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      val a = variable.now()
      // val values = sealedTrait.subtypes.map(_.typeInfo.short)
      // div(
      //   factory
      //     .renderSelect(_ => ())
      //     .amend(
      //       sealedTrait.subtypes
      //         .map(_.typeInfo.short)
      //         .map { label =>
      //           factory.renderOption(
      //             label,
      //             values
      //               .map(_.toString)
      //               .indexOf(label),
      //             label == variable.now().toString
      //           )
      //         }
      //         .toSeq
      //     ),
      sealedTrait.choose(a) { sub =>
        val va = Var(sub.cast(a))
        sub.typeclass
          .render(
            va,
            () => {
              variable.set(va.now())
              syncParent()
            }
          )
          .amend(
            idAttr := sub.typeInfo.short
          )
      }
      // )
  }

  def getSubtypeLabel[T](sub: Subtype[Typeclass, T, ?]): String =
    sub.annotations
      .collectFirst { case label: FieldName => label.value }
      .getOrElse(titleCase(sub.typeInfo.short))

  /** someParameterName -> Some Parameter Name camelCase -> Title Case
    */
  private def titleCase(string: String): String =
    string.split("(?=[A-Z])").map(_.capitalize).mkString(" ")

}

/** Use this form to render a string that can be converted to A, can be used for
  * Opaque types.
  */
def stringForm[A](to: String => A) = new Form[A]:
  override def render(
      variable: Var[A],
      syncParent: () => Unit
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
      syncParent: () => Unit
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

def secretForm[A <: String](to: String => A) = new Form[A]:
  override def render(
      variable: Var[A],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    factory.renderSecret.amend(
      value <-- variable.signal,
      onInput.mapToValue.map(to) --> { v =>
        variable.set(v)
        syncParent()
      }
    )

def enumForm[A](values: Array[A], f: Int => A) = new Form[A] {

  override def render(
      variable: Var[A],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    val valuesLabels = values.map(_.toString)
    div(
      factory
        .renderSelect(idx => variable.set(f(idx)))
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

}
