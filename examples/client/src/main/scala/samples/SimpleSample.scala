package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import be.doeraene.webcomponents.ui5.Title

import demo.facades.highlightjs.hljs

val simple = {
  val simpleVar = Var(Cat("Scala le chat", 6))
  Sample(
    "Simple", {
      table(
        tr(
          td(
            div(
              simpleVar.asForm
            )
          ),
          td(
            div(
              marginTop := "1em",
              overflowX := "auto",
              border := "0.0625rem solid #C1C1C1",
              backgroundColor := "#f5f6fa",
              padding := "1rem",
              Title.h3("Code"),
              pre(
                code(
                  className := "language-scala",
                  // demoPanelInfo.maybeStripIndentCommon
                  //   .map(_ ++ "\n\n")
                  //   .getOrElse("") ++
                  //   thisExampleInfo.stripIndent,
                  onMountCallback(ctx =>
                    hljs.highlightElement(ctx.thisNode.ref)
                  ),
                  """
                  |case class Cat(name: String, weight: Int, kind: Boolean = true)
                  |
                  |val simpleVar = Var(Cat("Scala le chat", 6))
                  |
                  |simpleVar.asForm
                  """.stripMargin
                )
              )
            )
          )
        ),
        tr(
          td(
            colSpan := 2,
            child <-- simpleVar.signal.map { item =>
              div(
                s"$item"
              )
            }
          )
        )
      )
    }
  )
}
