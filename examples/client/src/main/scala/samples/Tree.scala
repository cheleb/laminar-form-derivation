package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

enum Tree[+T]:
  case Empty extends Tree[Nothing]
  case Node(value: T, left: Tree[T] = Empty, right: Tree[T] = Empty)
object Tree:
  def homomorphism[A, B](f: A => B)(tree: Tree[A]): Tree[B] =
    tree match
      case Empty => Empty
      case Node(value, left, right) =>
        Node(f(value), homomorphism(f)(left), homomorphism(f)(right))
  def isomorphic[A, B](f: A => B, g: B => A)(tree: Tree[A]): Tree[B] =
    homomorphism(f)(tree)
  def isSameStructure(tree1: Tree[?], tree2: Tree[?]): Boolean =
    (tree1, tree2) match
      case (Empty, Empty)         => true
      case (Node(_, _, _), Empty) => false
      case (Empty, Node(_, _, _)) => false
      case (Node(_, left1, right1), Node(_, left2, right2)) =>
        isSameStructure(left1, left2) && isSameStructure(right1, right2)
implicit def treeInstance[A](using
    default: Defaultable[A]
)(using Form[A]): Form[Tree[A]] =
  new Form[Tree[A]] {
    override def isAnyRef = true
    override def render(
        variable: Var[Tree[A]],
        syncParent: () => Unit,
        values: List[Tree[A]]
    )(using WidgetFactory): HtmlElement =
      variable.now() match
        case Tree.Empty =>
          button(
            "Add me",
            onClick.mapTo(
              Tree.Node(default.default, Tree.Empty, Tree.Empty)
            ) --> variable.writer
          )
        case Tree.Node(value, left, right) =>
          div(
            button("drop", onClick.mapTo(Tree.Empty) --> variable.writer),
            summon[Form[Tree.Node[A]]].render(
              variable.asInstanceOf[Var[Tree.Node[A]]]
            )
          )
  }

val tree = Sample(
  "Tree", {

    import Tree.*

    case class Person(name: String, age: Int)
    object Person {
      given Defaultable[Person] with
        def default = Person("--", 0)
    }
    val treeVar2 = Var(
      Node(Person("agnes", 40), Empty, Empty)
    )
    div(
      child <-- treeVar2.signal.map { item =>
        div(
          s"$item zozo"
        )
      },
      child <-- treeVar2.signal
        .distinctByFn(Tree.isSameStructure)
        .map { item =>
          Form.renderVar(treeVar2)
        }
    )
  }
)
