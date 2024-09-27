package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*

import scala.util.Try
import com.raquo.airstream.state.Var
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.HTMLElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import magnolia1.SealedTrait.Subtype
import java.time.LocalDate
import io.github.iltotore.iron.*

import config.PanelConfig

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

  /*


  <div style="width: 100%; overflow: hidden;">
     <div style="width: 600px; float: left;"> Left </div>
     <div style="margin-left: 620px;"> Right </div>
  </div>


   */
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

  type Typeclass[T] = Form[T]

  /** Render a variable with a form.
    *
    * @param v
    *   the variable to render
    * @param syncParent
    *   a function to sync the parent state
    * @param factory
    *   the widget factory
    * @param fa
    *   the form for the variable, either given or derived by magnolia <3
    * @tparam A
    *   the type of the variable
    * @return
    */
  def renderVar[A](v: Var[A], syncParent: () => Unit = () => ())(using
      WidgetFactory
  )(using
      fa: Form[A]
  ): ReactiveHtmlElement[HTMLElement] =
    fa.render(v, syncParent)

  /** Form for an Iron type. This is a form for a type that can be validated
    * with an Iron type.
    * @param validator
    *   the Iron type validator
    * @tparam T
    *   the base type of the Iron type
    * @tparam C
    *   the type of the Iron type contraint
    */
  given [T, C](using
      validator: IronTypeValidator[T, C],
      widgetFactory: WidgetFactory
  ): Form[IronType[T, C]] =
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
          widgetFactory.renderText
            .amend(
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
        validator.validate(str) match
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

  /** Form for a Nothing, not sure it is still really needed :-/
    */
  given Form[Nothing] = new Form[Nothing] {
    override def render(
        variable: Var[Nothing],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      div()
  }

  /** Form for a Boolean.
    *
    * Basically a checkbox.
    */
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

  /** Form for an Double.
    */
  given Form[Double] = numericForm(_.toDoubleOption, 0)

  /** Form for an Int.
    */
  given Form[Int] = numericForm(_.toIntOption, 0)

  /** Form for an Float.
    */
  given Form[Float] = numericForm(_.toFloatOption, 0)

  /** Form for an Long.
    */
  given Form[Long] = numericForm(_.toLongOption, 0)

  /** Form for a BigInt.
    */
  given Form[BigInt] =
    numericForm(str => Try(BigInt(str)).toOption, BigInt(0))
    /** Form for a BigDecimal.
      */
  given Form[BigDecimal] =
    numericForm(str => Try(BigDecimal(str)).toOption, BigDecimal(0))

  /** Form for a either of L or R
    *
    * @param lf
    *   the left form for a L, given or derived by magnolia
    * @param rf
    *   the right form for a R, given or derived by magnolia
    * @param ld
    *   the default value for a L
    * @param rd
    *   the default value for a R
    * @return
    */
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

  /** Form for an Option[A]
    *
    * Render with clear button if the value is Some, else render with a set new
    * value button.
    * @param fa
    *   the form for A
    * @param d
    *   the default value for A
    * @return
    */
  given optionOfA[A](using
      fa: Form[A],
      d: Defaultable[A]
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

  /** Form for a List[A]
    * @param fa
    *   the form for A
    * @param idOf
    *   a function to get the id of an A, important for the split function.
    * @return
    */

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

  /** Form for a LocalDate
    *
    * Render a date picker. // FIXME should be able to set the format
    */
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

  def join[A](
      caseClass: CaseClass[Typeclass, A]
  ): Form[A] = new Form[A] {

    override def render(
        variable: Var[A],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement = {

      val panel =
        caseClass.annotations.find(_.isInstanceOf[Panel]) match
          case None =>
            caseClass.annotations.find(_.isInstanceOf[NoPanel]) match
              case None =>
                PanelConfig(Some(caseClass.typeInfo.short), true)
              case Some(annot) =>
                val asTable = annot.asInstanceOf[NoPanel].asTable
                PanelConfig(None, asTable)

          case Some(value) =>
            val panel = value.asInstanceOf[Panel]
            PanelConfig(Option(panel.name), panel.asTable)

      def renderAsTable() =
        factory
          .renderPanel(panel.label)
          .amend(
            className := "panel panel-default",
            table(
              caseClass.params.map { param =>

                val isOption =
                  param.deref(variable.now()).isInstanceOf[Option[?]]

                val fieldName = param.annotations
                  .find(_.isInstanceOf[FieldName]) match
                  case None => param.label
                  case Some(value) =>
                    value.asInstanceOf[FieldName].value
                tr(
                  td(
                    factory.renderLabel(
                      !isOption,
                      fieldName
                    )
                  ),
                  td(
                    param.typeclass
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
                  )
                )
              }.toSeq
            )
          )

      def renderAsPanel() =
        factory
          .renderPanel(panel.label)
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

      if panel.asTable then renderAsTable()
      else renderAsPanel()
    }
  }

  def split[A](sealedTrait: SealedTrait[Form, A]): Form[A] = new Form[A] {

    override def render(
        variable: Var[A],
        syncParent: () => Unit
    )(using factory: WidgetFactory): HtmlElement =
      val a = variable.now()
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
