package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

object tree {
  enum Tree[+T]:
    case Empty extends Tree[Nothing]
    case Node(value: T, left: Tree[T], right: Tree[T])

  implicit def treeInstance: Form[Tree[Int]] = Form.derived
  implicit def treeInstance2: Form[Tree[Nothing]] =
    new Form[Tree[Nothing]] {
      override def isAnyRef = true
    }

  import Tree.*

  val treeVar = Var(Node(1, Node(2, Empty, Empty), Node(3, Empty, Empty)))
  // treeVar = Var(Empty)

  val component = div(
    child <-- treeVar.signal.map { item =>
      div(
        s"$item zozo"
      )
    },
    Form.renderVar(treeVar)
  )
}
