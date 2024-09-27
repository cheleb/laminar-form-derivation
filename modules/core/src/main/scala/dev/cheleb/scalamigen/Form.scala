package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*

import scala.util.Try
import com.raquo.airstream.state.Var
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.HTMLElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import magnolia1.SealedTrait.Subtype
import scala.deriving.Mirror
import java.time.LocalDate
import io.github.iltotore.iron.*

/** A form for a type A.
  */
trait Form[A] { self =>

//  def isAnyRef = false

  /** Parse a string and return an Option[A].
    *
    * @param s
    * @return
    */
  def fromString(s: String): Option[A] = None

  /** Parse a string and set t he variable to the parsed value or set the
    * errorVar to an error message.
    *
    * @param s
    * @param variable
    * @param errorVar
    */
  def fromString(s: String, variable: Var[A], errorVar: Var[String]): Unit = ()

  def toString(a: A) = a.toString

  /** Render a form for a variable.
    *
    * Sometimes the form is a part of a larger form and the parent form needs to
    * be updated when the variable changes. This is the purpose of the
    * syncParent function.
    *
    * @param variable
    *   the variable to render
    * @param syncParent
    *   a function to sync the parent state
    * @param factory
    *   the widget factory
    * @return
    */
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

object Form {
  def renderVar[A](v: Var[A], syncParent: () => Unit = () => ())(using
      WidgetFactory
  )(using
      fa: Form[A]
  ): ReactiveHtmlElement[HTMLElement] =
    fa.render(v, syncParent)

  /** Use this form to render a string that can be converted to A, can be used
    * for Opaque types.
    */

  /** Form for an Iron type. This is a form for a type that can be validated
    * with an Iron type.
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

  // given

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

}

object FormDerive extends AutoDerivation[Form] {

  type Typeclass[T] = Form[T]
  def join[A](
      caseClass: CaseClass[Typeclass, A]
  ): Form[A] = new Form[A] {

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

extension [A]($ : Form.type)(using Mirror.Of[A])
  inline def derived: Form[A] = FormDerive.derived[A]
