package samples

import dev.cheleb.scalamigen.*

import com.raquo.laminar.api.L.*

/** Poc poc =D
  */
def tree(using
    wf: WidgetFactory
): Sample = {

  enum Tree[+T]:
    case Empty extends Tree[Nothing]
    case Node(value: T, left: Tree[T], right: Tree[T])
  object Tree:
    def isSameStructure(tree1: Tree[?], tree2: Tree[?]): Boolean =
      (tree1, tree2) match
        case (Empty, Empty)                                   => true
        case (Node(_, _, _), Empty)                           => false
        case (Empty, Node(_, _, _))                           => false
        case (Node(_, left1, right1), Node(_, left2, right2)) =>
          isSameStructure(left1, left2) && isSameStructure(right1, right2)

    given treeInstance[A](using
        default: Defaultable[A]
    )(using Form[A]): Form[Tree[A]] = new Form[Tree[A]] { self =>
      override def render(
          path: List[Symbol],
          variable: Var[Tree[A]]
      )(using WidgetFactory, EventBus[(String, ValidationEvent)]): HtmlElement =
        variable.now() match
          case Tree.Empty =>
            button(
              "Add me",
              onClick.mapToUnit --> { _ =>
                variable.set(Tree.Node(default.default, Tree.Empty, Tree.Empty))
              }
            )
          case Tree.Node(value, left, right) =>
            div(
              button(
                "drop",
                onClick.mapToUnit --> { _ =>
                  variable.set(Tree.Empty)
                }
              ), {
                val vVar = Var(value)
                val lVar = Var(left)
                val rVar = Var(right)

                Seq(
                  Form.renderVar(
                    path :+ Symbol("value"),
                    vVar
                  ),
                  div(
                    "left",
                    Form.renderVar(
                      path :+ Symbol("left"),
                      lVar
                    )
                  ),
                  div(
                    "right",
                    Form.renderVar(
                      path :+ Symbol("right"),
                      rVar
                    )
                  )
                )
              }
            )
    }

  import Tree.*
  case class Person(name: String, age: Int)
  object Person {
    given Defaultable[Person] with
      def default = Person("--", 0)
  }
  val treeVar2 = Var(
    Node(
      Person("agnes", 50),
      Node(Person("Zozo", 53), Empty, Empty),
      Empty
    )
  )
  Sample(
    "Tree", {
      div(
        child <-- treeVar2.signal
          .distinctByFn(Tree.isSameStructure)
          .map { _ =>
            val b = new EventBus[(String, ValidationEvent)]()
            treeVar2.asForm(b)
          }
      )
    },
    div(
      child <-- treeVar2.signal.map { item =>
        div(
          s"$item zozo"
        )
      }
    )
  )
}
