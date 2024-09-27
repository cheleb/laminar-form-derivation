package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val simple = {
  val eitherVar = Var(Cat("Scala le chat", 6))
  Sample(
    "Simple", {

      div(
        child <-- eitherVar.signal.map { item =>
          div(
            s"$item"
          )
        },
        eitherVar.asForm
      )
    }
  )
}
