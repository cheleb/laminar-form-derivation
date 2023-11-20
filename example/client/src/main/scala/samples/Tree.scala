package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

object tree {
  enum Tree[+T]:
    case Empty extends Tree[Nothing]
    case Node(value: T, left: Tree[T] = Empty, right: Tree[T] = Empty)

  val nodeInstance: Form[Tree.Node[Int]] = Form.derived
  implicit def treeInstance: Form[Tree[Int]] =
    new Form[Tree[Int]] {
      override def isAnyRef = true

      override def render(
          variable: Var[Tree[Int]],
          syncParent: () => Unit,
          values: List[Tree[Int]]
      ): HtmlElement =
        variable.now() match
          case Tree.Empty =>
            button(
              "Add",
              onClick --> Observer(_ =>
                variable.set(Tree.Node(0, Tree.Empty, Tree.Empty))
              )
            )

          case Tree.Node(value, left, right) =>
            nodeInstance.render(
              variable.asInstanceOf[Var[Tree.Node[Int]]],
              syncParent,
              Nil
            )
    }

  import Tree.*

  val treeVar = Var(
    Node(1, Node(2, Node(3, Empty, Empty), Empty), Node(4, Empty, Empty))
  )

  val component = div(
    child <-- treeVar.signal.map { item =>
      div(
        div(
          s"$item zozo"
        ),
        Form.renderVar(treeVar)
      )
    }
  )
}
