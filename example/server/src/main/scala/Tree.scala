package tree

import magnolia1.*

enum Tree[+T] derives Print:
  case Branch(left: Tree[T], right: Tree[T])
  case Leaf(value: T)

trait Print[T] {
  extension (x: T) def print: String
}

object Print extends AutoDerivation[Print]:
  def join[T](ctx: CaseClass[Print, T]): Print[T] = value =>
    ctx.params
      .map { param =>
        param.typeclass.print(param.deref(value))
      }
      .mkString(s"${ctx.typeInfo.short}(", ",", ")")

  override def split[T](ctx: SealedTrait[Print, T]): Print[T] = value =>
    ctx.choose(value) { sub => sub.typeclass.print(sub.cast(value)) }

  given Print[Int] = _.toString

//@main
def run =
  println(Tree.Branch(Tree.Leaf(1), Tree.Leaf(2)).print)
