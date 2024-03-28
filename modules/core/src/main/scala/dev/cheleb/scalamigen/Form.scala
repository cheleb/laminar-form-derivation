package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*

import scala.util.Try
import com.raquo.airstream.state.Var
import org.scalajs.dom.HTMLDivElement
import magnolia1.SealedTrait.SubtypeValue

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
  ) = {
    fa.render(v, syncParent)
  }
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

  def split[A](sealedTrait: SealedTrait[Form, A]): Form[A] = new Form[A] {

    override def isAnyRef: Boolean = true
    override def render(
        variable: Var[A],
        syncParent: () => Unit,
        values: List[A] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      if sealedTrait.isEnum then
        if values.isEmpty then
          sealedTrait
            .choose(variable.now()) { case o =>
              val vo = Var(o.value)
              o.typeclass.render(
                vo,
                () =>
                  variable.set(vo.now())
                  syncParent()
              )
            }
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
      else div("Not an enum")

  }

}
