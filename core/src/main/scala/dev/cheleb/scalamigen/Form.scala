package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*
import scala.CanEqual.derived
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import java.util.UUID
import scala.util.Random

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import scala.util.Try
import com.raquo.airstream.state.Var
import com.raquo.airstream.core.Source
import com.raquo.laminar.nodes.ReactiveElement
import org.scalajs.dom.HTMLDivElement
import magnolia1.SealedTrait.SubtypeValue

trait IronTypeValidator[T, C] {
  def validate(a: String): Either[String, IronType[T, C]]
}

trait Defaultable[A] {
  def default: A
  def label: String = default.getClass.getSimpleName()
}

trait Form[A] { self =>

  def isAnyRef = false

//  def default: A

  def fromString(s: String): Option[A] = None
  def fromString(s: String, variable: Var[A], errorVar: Var[String]): Unit = ()

  def toString(a: A) = a.toString

  def render(
      variable: Var[A]
  ): HtmlElement =
    render(variable, () => ())

  def render(
      variable: Var[A],
      syncParent: () => Unit,
      values: List[A] = List.empty
  ): HtmlElement =
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
    ): HtmlElement =
      div(
        div(
          Label(_.required := required, _.showColon := false, name)
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
    ): HtmlElement =
      self.render(variable.zoom(from)(to), syncParent, values.map(from))
  }

}

object Form extends AutoDerivation[Form] {

  def renderVar[A](v: Var[A], syncParent: () => Unit = () => ())(using
      fa: Form[A]
  ) = {
    fa.render(v, syncParent)
  }
  def join[A](caseClass: CaseClass[Typeclass, A]): Form[A] = new Form[A] {

    override def isAnyRef: Boolean = true
    override def render(
        variable: Var[A],
        syncParent: () => Unit = () => (),
        values: List[A] = List.empty
    ): HtmlElement =
      Panel(
        _.id := caseClass.typeInfo.full,
        _.headerText := caseClass.typeInfo.full,
        _.headerLevel := TitleLevel.H3,
        caseClass.params.map { param =>
          val isOption = param.deref(variable.now()).isInstanceOf[Option[_]]

          val enumValues =
            if param.annotations.isEmpty then List.empty[A]
            else if param.annotations(0).isInstanceOf[EnumValues[_]] then
              param.annotations(0).asInstanceOf[EnumValues[A]].values.toList
            else List.empty[A]

          param.typeclass
            .labelled(param.label, !isOption)
            .render(
              variable.zoom(a => param.deref(a))((_, value) =>
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
    ): HtmlElement =
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
            Select(
              _.events.onChange
                .map(_.detail.selectedOption.dataset) --> { ds =>
                ds.get("idx").foreach(idx => variable.set(values(idx.toInt)))
                syncParent()
              },
              sealedTrait.subtypes
                .map(_.typeInfo.short)
                .filter(valuesLabels.contains(_))
                .map { label =>
                  Select.option(
                    label,
                    dataAttr("idx") := values
                      .map(_.toString)
                      .indexOf(label)
                      .toString(),
                    _.selected <-- variable.signal.map(
                      _.toString() == label
                    )
                  )
                }
                .toSeq
            )
          )
      else div("Not an enum")

  }

}
