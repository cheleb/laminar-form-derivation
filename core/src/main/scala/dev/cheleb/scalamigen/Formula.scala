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

trait IronTypeValidator[T, C] {
  def validate(a: String): Either[String, IronType[T, C]]
}

trait Defaultable[A] {
  def default: A
}

trait Form[A] { self =>

  def isAnyRef = false

  def fromString(s: String): Option[A] = None
  def fromString(s: String, variable: Var[A], errorVar: Var[String]): Unit = ()

  def toString(a: A) = a.toString

  def render(
      variable: Var[A],
      syncParent: () => Unit
  ): HtmlElement =
    val errorVar = Var("")
    div(
      div(child <-- errorVar.signal.map { item =>
        div(
          s"$item"
        )
      }),
      input(
        tpe("number"),
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
    override def render(variable: Var[A], syncParent: () => Unit): HtmlElement =
      div(
        div(
          Label(_.required := required, _.showColon := false, name)
        ),
        div(
          self.render(variable, syncParent)
        )
      )

  }
  def xmap[B](to: (B, A) => B)(from: B => A): Form[B] = new Form[B] {
    override def render(variable: Var[B], syncParent: () => Unit): HtmlElement =
      self.render(variable.zoom(from)(to), syncParent)
  }
//  def render(variable: Var[A], syncParent: () => Unit): HtmlElement
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
        syncParent: () => Unit = () => ()
    ): HtmlElement =
      Panel(
        _.id := caseClass.typeInfo.full,
        _.headerText := caseClass.typeInfo.full,
        _.headerLevel := TitleLevel.H3,
        caseClass.params.map { param =>
          val isOption = param.deref(variable.now()).isInstanceOf[Option[_]]

          param.typeclass
            .labelled(param.label, !isOption)
            .render(
              variable.zoom(a => param.deref(a))((_, value) =>
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

  given listOfA[A](using fa: Form[A]): Form[List[A]] =
    new Form[List[A]] {

      override def render(
          variable: Var[List[A]],
          syncParent: () => Unit
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
    override def render(variable: Var[A], syncParent: () => Unit): HtmlElement =
      sealedTrait.choose(variable.now()) { sub =>
        sub.typeclass.render(sub.cast(variable.now()))
      }
  }

  given string: Form[String] with
    override def render(
        variable: Var[String],
        syncParent: () => Unit
    ): HtmlElement =
      Input(
        _.showClearIcon := true,
        value <-- variable.signal,
        onInput.mapToValue --> { v =>
          variable.set(v)
          syncParent()
        }
      )

  given int: Form[Int] = new Form[Int] { self =>
    override def fromString(s: String): Option[Int] = s.toIntOption
    override def render(
        variable: Var[Int],
        syncParent: () => Unit
    ): HtmlElement =
      input(
        tpe("number"),
        controlled(
          value <-- variable.signal.map { str =>
            str.toString()
          },
          onInput.mapToValue --> { v =>
            v.toIntOption.foreach(variable.set)
            syncParent()
          }
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
