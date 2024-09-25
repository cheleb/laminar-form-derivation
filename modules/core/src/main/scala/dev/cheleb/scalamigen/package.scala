package dev.cheleb.scalamigen

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

import com.raquo.laminar.api.L.*

import scala.util.Try
import com.raquo.airstream.state.Var

import java.time.LocalDate

/** Default value for Int is 0.
  */
given Defaultable[Int] with
  def default = 0

/** Default value for String is "".
  */
given Defaultable[String] with
  def default = ""

/** Default value for [Iron type Double
  * positive](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint/numeric$.html#Positive-0)
  * is 0.0.
  */
given Defaultable[IronType[Double, Positive]] with
  def default = 1.0.refineUnsafe[Positive]

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

/** Validator for [Iron type Double
  * positive](https://iltotore.github.io/iron/io/github/iltotore/iron/constraint/numeric$.html#Positive-0).
  */
given IronTypeValidator[Double, Positive] with
  def validate(a: String): Either[String, IronType[Double, Positive]] =
    a.toDoubleOption match
      case None         => Left("Not a number")
      case Some(double) => double.refineEither[Positive]

/** Form for an Iron type. This is a form for a type that can be validated with
  * an Iron type.
  */
given [T, C](using fv: IronTypeValidator[T, C]): Form[IronType[T, C]] =
  new Form[IronType[T, C]] {

    override def render(
        variable: Var[IronType[T, C]],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =

      val errorVar = Var("")
      div(
        div(child <-- errorVar.signal.map { item =>
          div(
            s"$item"
          )
        }),
        input(
          // _.showClearIcon := true,
          backgroundColor <-- errorVar.signal.map {
            case "" => "white"
            case _  => "red"
          },
          value <-- variable.signal.map(toString(_)),
          onInput.mapToValue --> { str =>
            fromString(str, variable, errorVar)

          }
        )
      )

    override def fromString(
        str: String,
        variable: Var[IronType[T, C]],
        errorVar: Var[String]
    ): Unit =
      fv.validate(str) match
        case Left(error) =>
          errorVar.set(error)
        case Right(value) =>
          errorVar.set("")
          variable.set(value)
  }

/** Form for to a string, aka without validation.
  */
given Form[String] with
  override def render(
      variable: Var[String],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    factory.renderText
      .amend(
        value <-- variable.signal,
        onInput.mapToValue --> { v =>
          variable.set(v)
          syncParent()
        }
      )

given Form[Nothing] = new Form[Nothing] {
  override def render(
      variable: Var[Nothing],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    div()
}

given Form[Boolean] = new Form[Boolean] {
  override def render(
      variable: Var[Boolean],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    div(
      factory.renderCheckbox
        .amend(
          checked <-- variable.signal,
          onChange.mapToChecked --> { v =>
            variable.set(v)
            syncParent()
          }
        )
    )
}
given Form[Double] = numericForm(_.toDoubleOption, 0)
given Form[Int] = numericForm(_.toIntOption, 0)
given Form[Float] = numericForm(_.toFloatOption, 0)
given Form[BigInt] =
  numericForm(str => Try(BigInt(str)).toOption, BigInt(0))
given Form[BigDecimal] =
  numericForm(str => Try(BigDecimal(str)).toOption, BigDecimal(0))

//given

given eitherOf[L, R](using
    lf: Form[L],
    rf: Form[R],
    ld: Defaultable[L],
    rd: Defaultable[R]
): Form[Either[L, R]] =
  new Form[Either[L, R]] {
    override def render(
        variable: Var[Either[L, R]],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =

      val (vl, vr) = variable.now() match
        case Left(l) =>
          (Var(l), Var(rd.default))
        case Right(r) =>
          (Var(ld.default), Var(r))

      div(
        span(
          factory
            .renderLink(
              ld.label,
              onClick.mapTo(Left(vl.now())) --> variable.writer
            ),
          "----",
          factory.renderLink(
            rd.label,
            onClick.mapTo(
              Right(vr.now())
            ) --> variable.writer
          )
        ),
        div(
          display <-- variable.signal.map {
            case Left(_) => "block"
            case _       => "none"
          },
          lf.render(vl, () => variable.set(Left(vl.now())))
        ),
        div(
          display <-- variable.signal.map {
            case Left(_) => "none"
            case _       => "block"
          },
          rf.render(vr, () => variable.set(Right(vr.now())))
        )
      )

  }

given optionOfA[A](using
    d: Defaultable[A],
    fa: Form[A]
): Form[Option[A]] =
  new Form[Option[A]] {
    override def render(
        variable: Var[Option[A]],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      val a = variable.zoom {
        case Some(a) =>
          a
        case None => d.default
      } { case (_, a) =>
        Some(a)
      }
      a.now() match
        case null =>
          factory.renderButton.amend(
            "Set",
            onClick.mapTo(Some(d.default)) --> variable.writer
          )
        case _ =>
          div(
            div(
              display <-- variable.signal.map {
                case Some(_) => "block"
                case None    => "none"
              },
              fa.render(a, syncParent)
            ),
            div(
              factory.renderButton.amend(
                display <-- variable.signal.map {
                  case Some(_) => "none"
                  case None    => "block"
                },
                "Set",
                onClick.mapTo(Some(d.default)) --> variable.writer
              ),
              factory.renderButton.amend(
                display <-- variable.signal.map {
                  case Some(_) => "block"
                  case None    => "none"
                },
                "Clear",
                onClick.mapTo(None) --> variable.writer
              )
            )
          )
  }

given listOfA[A, K](using fa: Form[A], idOf: A => K): Form[List[A]] =
  new Form[List[A]] {

    override def render(
        variable: Var[List[A]],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      div(
        children <-- variable.split(idOf)((id, initial, aVar) => {
          div(
            idAttr := s"list-item-$id",
            div(
              fa.render(aVar, syncParent)
            )
          )
        })
      )
  }

given Form[LocalDate] = new Form[LocalDate] {
  override def render(
      variable: Var[LocalDate],
      syncParent: () => Unit
  )(using factory: WidgetFactory): HtmlElement =
    div(
      factory.renderDatePicker
        .amend(
          value <-- variable.signal.map(_.toString),
          onChange.mapToValue --> { v =>
            variable.set(LocalDate.parse(v))
            syncParent()
          }
        )
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

extension [A](v: Var[A])
  def asForm(using WidgetFactory, Form[A]) = Form.renderVar(v)
