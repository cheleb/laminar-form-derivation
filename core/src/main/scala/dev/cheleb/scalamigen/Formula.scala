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

  extension (a: A)
    def render: HtmlElement =
      self.render(Var(a), () => ())

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
  ) =
    fa.render(v, syncParent)

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

  given listOfA[A](using fa: Form[A]): Form[List[A]] =
    new Form[List[A]] {

      override def render(
          variable: Var[List[A]],
          syncParent: () => Unit,
          values: List[List[A]] = List.empty
      ): HtmlElement =

        def renderNewA(
            index: Int,
            initialAatIdx: (A, Int),
            aSignalAt: Signal[(A, Int)]
        ) =
          val va = Var(initialAatIdx._1)

          val formOfA =
            if (fa.isAnyRef)
              fa.render(va, () => variable.update(_.updated(index, va.now())))
            else
              fa.render(va, syncParent)
                .amend(
                  onInput.mapToValue --> { v =>
                    fa.fromString(v).foreach { v =>
                      variable.update(_.updated(index, v))
                    }
                  }
                )

          div(
            idAttr := s"list-item-$index",
            div(
              formOfA
            )
          )

        UList(
          width := "100%",
          _.id := "list-of-string",
          _.noDataText := "No  data",
          _.separators := ListSeparator.None,
          children <-- variable
            .zoom(_.zipWithIndex)((a, b) => b.map(_._1))
            .signal
            .split(_._2)(renderNewA)
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

  given Form[String] with
    override def render(
        variable: Var[String],
        syncParent: () => Unit,
        values: List[String] = List.empty
    ): HtmlElement =
      Input(
        _.showClearIcon := true,
        value <-- variable.signal,
        onInput.mapToValue --> { v =>
          variable.set(v)
          syncParent()
        }
      )

  def numericForm[A](f: String => Option[A], zero: A): Form[A] = new Form[A] {
    self =>
    override def fromString(s: String): Option[A] =
      f(s).orElse(Some(zero))
    override def render(
        variable: Var[A],
        syncParent: () => Unit,
        values: List[A] = List.empty
    ): HtmlElement =
      input(
        tpe("number"),
        controlled(
          value <-- variable.signal.map { str =>
            str.toString()
          },
          onInput.mapToValue --> { v =>
            fromString(v).foreach(variable.set)
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

  given eitherOf[L, R](using
      lf: Form[L],
      rf: Form[R],
      ld: Defaultable[L],
      rd: Defaultable[R]
  ): Form[Either[L, R]] =
    new Form[Either[L, R]] {
      override def render(
          variable: Var[Either[L, R]],
          syncParent: () => Unit,
          values: List[Either[L, R]] = List.empty
      ): HtmlElement =

        val (vl, vr) = variable.now() match
          case Left(l) =>
            (Var(l), Var(rd.default))
          case Right(r) =>
            (Var(ld.default), Var(r))

        div(
          span(
            Link(ld.label, onClick.mapTo(Left(vl.now())) --> variable.writer),
            "----",
            Link(
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
          syncParent: () => Unit,
          values: List[Option[A]] = List.empty
      ): HtmlElement =
        val a = variable.zoom {
          case Some(a) =>
            a
          case None => d.default
        } { case (_, a) =>
          Some(a)
        }
        a.now() match
          case null =>
            Button(
              _.design := ButtonDesign.Emphasized,
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
                Button(
                  display <-- variable.signal.map {
                    case Some(_) => "none"
                    case None    => "block"
                  },
                  _.design := ButtonDesign.Emphasized,
                  "Set",
                  onClick.mapTo(Some(d.default)) --> variable.writer
                ),
                Button(
                  display <-- variable.signal.map {
                    case Some(_) => "block"
                    case None    => "none"
                  },
                  _.design := ButtonDesign.Emphasized,
                  "Clear",
                  onClick.mapTo(None) --> variable.writer
                )
              )
            )
    }

}
