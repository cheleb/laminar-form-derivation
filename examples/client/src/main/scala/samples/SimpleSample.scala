package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

val simple: Sample = {

  case class Cat(
      name: String,
      weight: Int,
      hairsCount: BigInt :| GreaterEqual[100000],
      kind: Boolean = true
  )

  val simpleVar = Var(Cat("Scala le chat", 6, BigInt(100000).refineUnsafe))
  Sample(
    "Simple",
    simpleVar.asForm,
    div(
      child <-- simpleVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """
    |case class Cat(
    |                name: String,
    |                weight: Int,
    |                hairsCount: BigInt :| GreaterEqual[100000],
    |                kind: Boolean = true)
    |
    |val simpleVar = Var(Cat("Scala le chat", 6, BigInt(100000).refineUnsafe))
    |
    |simpleVar.asForm
    """.stripMargin
  )
}
