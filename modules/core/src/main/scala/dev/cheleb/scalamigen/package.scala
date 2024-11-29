package dev.cheleb.scalamigen

import com.raquo.airstream.state.Var

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.HTMLElement

/** A form for a type A, no validation. Convenient to use for Opaque types. If
  * you need validation, use a Form with a ValidationEvent.
  */
def stringForm[A](to: String => A) = new Form[A]:
  override def render(
      path: List[Symbol],
      variable: Var[A],
      syncParent: () => Unit
  )(using
      factory: WidgetFactory,
      errorBus: EventBus[(String, ValidationEvent)]
  ): HtmlElement =
    factory.renderText.amend(
      value <-- variable.signal.map(_.toString),
      onInput.mapToValue.map(to) --> { v =>
        variable.set(v)
        syncParent()
      }
    )
def secretForm[A <: String](to: String => A) = new Form[A]:
  override def render(
      path: List[Symbol],
      variable: Var[A],
      syncParent: () => Unit
  )(using
      factory: WidgetFactory,
      errorBus: EventBus[(String, ValidationEvent)]
  ): HtmlElement =
    factory.renderSecret.amend(
      value <-- variable.signal,
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
      path: List[Symbol],
      variable: Var[A],
      syncParent: () => Unit
  )(using
      factory: WidgetFactory,
      errorBus: EventBus[(String, ValidationEvent)]
  ): HtmlElement =
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

def enumForm[A](values: Array[A], f: Int => A) = new Form[A] {

  override def render(
      path: List[Symbol],
      variable: Var[A],
      syncParent: () => Unit
  )(using
      factory: WidgetFactory,
      errorBus: EventBus[(String, ValidationEvent)]
  ): HtmlElement =
    val valuesLabels = values.map(_.toString)
    div(
      factory
        .renderSelect { idx =>
          variable.set(f(idx))
          syncParent()
        }
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

/** A form for a type A, no validation. Convenient to use for Opaque types. If
  * you need validation, use a Form with a ValidationEvent.
  */
def stringFormWithValidation[A](using
    validator: Validator[A]
) = new Form[A]:
  override def render(
      path: List[Symbol],
      variable: Var[A],
      syncParent: () => Unit
  )(using
      factory: WidgetFactory,
      errorBus: EventBus[(String, ValidationEvent)]
  ): HtmlElement =
    factory.renderText.amend(
      value <-- variable.signal.map(_.toString),
      onInput.mapToValue.map(validator.validate) --> {
        case Right(v) =>
          variable.set(v)
          errorBus.emit(
            (path.key, ValidEvent)
          )
          syncParent()
        case Left(err) =>
          errorBus.emit(
            (path.key, InvalideEvent(err))
          )
      }
    )

/** Extension methods for the Var class.
  */
extension [A](va: Var[A])
  /** Render a form for the variable.
    *
    * @param wf
    *   The widget factory.
    * @return
    */
  def asForm(using wf: WidgetFactory)(using
      Form[A]
  ): ReactiveHtmlElement[HTMLElement] = {
    val errorBus = new EventBus[(String, ValidationEvent)]()
    div(
      Form
        .renderVar(va, () => ())(using wf, errorBus)
        .amend(cls := "srf-form"),
      child <-- errorBus
        .errors((field, message) =>
          div(
            s"$field: $message"
          )
        )
    )

  }

  /** Render a form for the variable.
    *
    * @param errorBus
    *   The error bus.
    * @param wf
    *   The widget factory.
    * @return
    */
  def asForm(errorBus: EventBus[(String, ValidationEvent)])(using
      wf: WidgetFactory
  )(using Form[A]): ReactiveHtmlElement[HTMLElement] =
    Form
      .renderVar(va, () => ())(using wf, errorBus)
      .amend(cls := "srf-form")

  /** Buid an error bus for the variable that will be used to display errors.
    *
    * @return
    */
  def errorBus = new EventBus[(String, ValidationEvent)]
