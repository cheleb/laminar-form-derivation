package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.*
import magnolia1.*
import scala.CanEqual.derived
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.*
import java.util.UUID
import scala.util.Random
import com.raquo.domtypes.generic.keys.HtmlAttr
import com.raquo.domtypes.generic.defs.attrs.HtmlAttrs

trait Editable[A] {
  def render(a: A, update: Observer[A]): HtmlElement
}

trait Defaultable[A] {
  def default: A
}

sealed trait Form[A] { self =>

  extension (a: A)
    def render: HtmlElement =
      self.render(Var(a))

  given Owner = unsafeWindowOwner

  def labelled(name: String, required: Boolean): Form[A] = new Form[A] {
    def render(variable: Var[A]): HtmlElement =
      div(
        div(
          Label(_.required := required, _.showColon := false, name)
        ),
        div(
          self.render(variable)
        )
      )

  }
  def xmap[B](to: A => B)(from: B => A): Form[B] = new Form[B] {
    def render(variable: Var[B]): HtmlElement =
      self.render(variable.zoom(from)(to))
  }
  def render(variable: Var[A]): HtmlElement
}

object Form extends AutoDerivation[Form] {

  inline def renderVar[A](v: Var[A])(using fa: Form[A]) =
    fa.render(v)

  def join[A](caseClass: CaseClass[Typeclass, A]): Form[A] = new Form[A] {
    def render(variable: Var[A]): HtmlElement =
      Panel(
        _.id := caseClass.typeInfo.full,
        _.headerText := caseClass.typeInfo.full,
        _.headerLevel := TitleLevel.H3,
        caseClass.params.map { param =>
          val isOption = param.deref(variable.now()).isInstanceOf[Option[_]]

          param.typeclass
            .labelled(param.label, !isOption)
            .render(
              variable.zoom(a => param.deref(a))(value =>
                caseClass.construct { p =>
                  if (p.label == param.label) value
                  else p.deref(variable.now())
                }
              )(unsafeWindowOwner)
            )
            .amend(
              idAttr := param.label
            )
        }.toSeq
      )
  }

  inline given optionOfA[A](using
      d: Defaultable[A],
      fa: Form[A]
  ): Form[Option[A]] =
    new Form[Option[A]] {
      def render(variable: Var[Option[A]]): HtmlElement =
        val a = variable.zoom {
          case Some(a) =>
            a
          case None => d.default
        } { case a =>
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
                fa.render(a)
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

  inline given Editable[Int] with
    def render(a: Int, update: Observer[Int]): HtmlElement =
      UList.item(
        input(
          tpe("number"),
          value <-- Var(a.toString),
          onInput.mapToValue.map(_.toInt) --> update,
          a
        )
      )

  inline given listOfA[A](using fa: Form[A]): Form[List[A]] =
    new Form[List[A]] {

      def render(variable: Var[List[A]]): HtmlElement =

        def renderNewA(
            index: Int,
            initialAatIdx: (A, Int),
            aSignalAt: Signal[(A, Int)]
        ) =
          val va = Var(initialAatIdx._1)
          va.signal --> variable.updater[A] { (list, na) =>
            list.zipWithIndex.map { (a, idx) =>
              if (idx == index) na
              else a
            }
          }
          span(
            fa.render(va),
            Button(
              "Sync",
              onClick.mapTo(va.now()) --> variable.updater[A] { (list, na) =>
                list.zipWithIndex.map { (a, idx) =>
                  if (idx == index) na
                  else a
                }
              }
            )
          )

        UList(
          width := "100%",
          _.id := "list-of-string",
          _.noDataText := "No data",
          _.separators := ListSeparator.None,
          children <-- variable
            .zoom(_.zipWithIndex)(_.map(_._1))
            .signal
            .split(_._2)(renderNewA)
        )
    }

  inline given listOfA[A](using fa: Editable[A]): Form[List[A]] =
    new Form[List[A]] {

      def render(variable: Var[List[A]]): HtmlElement =

        def renderNewA(
            userId: Int,
            initialUser: (A, Int),
            userStream: Signal[(A, Int)]
        ) =
          fa.render(
            initialUser._1,
            Observer[A] { na =>
              variable.update { list =>
                list.zipWithIndex.map { (a, idx) =>
                  if (idx == userId) na
                  else a
                }
              }
            }
          )

        UList(
          width := "100%",
          _.id := "list-of-string",
          _.noDataText := "No data",
          _.separators := ListSeparator.None,
          children <-- variable
            .zoom(_.zipWithIndex)(_.map(_._1))
            .signal
            .split(_._2)(renderNewA)
        )
    }

  def split[A](sealedTrait: SealedTrait[Form, A]): Form[A] = new Form[A] {
    def render(variable: Var[A]): HtmlElement =
      sealedTrait.choose(variable.now()) { sub =>
        sub.typeclass.render(sub.cast(variable.now()))
      }
  }

  given string: Form[String] with
    def render(variable: Var[String]): HtmlElement =
      Input(
        _.showClearIcon := true,
        value <-- variable.signal,
        onInput.mapToValue --> variable.writer
      )

  given int: Form[Int] = new Form[Int] { self =>
    def render(variable: Var[Int]): HtmlElement =
      input(
        tpe("number"),
        controlled(
          value <-- variable.signal.map { str =>
            str.toString()
          },
          onInput.mapToValue.map(_.toInt) --> variable.writer
        )
      )
  }

  given optionString: Form[Option[String]] =
    new Form[Option[String]] {
      def render(variable: Var[Option[String]]): HtmlElement =
        variable.now() match {
          case Some(a) =>
            div(
              Input(
                _.showClearIcon := true,
                value <-- variable.signal.map(_.getOrElse("")),
                onInput.stopPropagation.mapToValue --> variable.someWriter
              ),
              Button(
                _.design := ButtonDesign.Emphasized,
                "Clear",
                onClick.mapTo(None) --> variable.writer
              )
            )
          case None =>
            Button(
              _.design := ButtonDesign.Emphasized,
              "Set",
              onClick.mapTo(Some("")) --> variable.writer
            )
        }

    }

  given optionInt: Form[Option[Int]] =
    new Form[Option[Int]] {
      def render(variable: Var[Option[Int]]): HtmlElement =
        variable.now() match {
          case Some(a) =>
            div(
              input(
                tpe("number"),
                controlled(
                  value <-- variable.signal.map { int =>
                    int.getOrElse(0).toString
                  },
                  onInput.mapToValue.map(_.toInt) --> variable.someWriter
                )
              ),
              Button(
                _.design := ButtonDesign.Emphasized,
                "Clear",
                onClick.mapTo(None) --> variable.writer
              )
            )
          case None =>
            Button(
              _.design := ButtonDesign.Emphasized,
              "Set",
              onClick.mapTo(Some(0)) --> variable.writer
            )
        }

    }
}
