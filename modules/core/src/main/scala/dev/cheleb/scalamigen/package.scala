package dev.cheleb.scalamigen

import com.raquo.airstream.state.Var

import com.raquo.laminar.api.L.*
import org.scalajs.dom.HTMLDivElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.airstream.core.Signal

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

extension [A](va: Var[A])
  def asForm(using wf: WidgetFactory)(using
      Form[A]
  ): ReactiveHtmlElement[HTMLDivElement] =
    val errorBus = new EventBus[(String, ValidationEvent)]()
    div(
      cls := "srf-form",
      Form.renderVar(va, () => ())(using wf, errorBus)
      //   child.maybe <-- _.d
    )
  def asForm(errorBus: EventBus[(String, ValidationEvent)])(using
      wf: WidgetFactory
  )(using Form[A]): ReactiveHtmlElement[HTMLDivElement] =
    div(
      cls := "srf-form",
      Form.renderVar(va, () => ())(using wf, errorBus)
      //   child.maybe <-- _.d
    )

  def isValid(using v: Validator[A]): Signal[Boolean] =
    va.signal.map(a => v.isValid(a))
