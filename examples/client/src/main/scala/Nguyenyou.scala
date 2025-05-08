package samples

import org.scalajs.dom
import com.raquo.laminar.api.L.*

import facades.highlightjs.{hljs, hljsScala}
import io.github.nguyenyou.ui5.webcomponents.laminar.*

case class Sample(
    name: String,
    component: HtmlElement,
    debug: HtmlElement,
    source: String = "TODO"
)

object App extends App {
  hljs.registerLanguage("scala", hljsScala)

  val sampleVar = Var(samples.simple)

  private def item(name: String) = SideNavigationItem(
    _.text := name,
    _.id := name
//    dataAttr("component-name") := name
  )()

  private val demos = Seq(
    samples.simple,
    samples.opaque,
    samples.either,
    samples.validation,
    samples.conditional,
    samples.enums,
    samples.sealedClasses,
    samples.person,
    samples.list,
    samples.tree,
    samples.adhoc
  )

  val myApp =
    div(
      display := "flex",
      div(
        paddingRight("2rem"),
        Title(
//          _.padding("0.5rem")
          // _.cursor := "pointer"
        )("Demos"),
        SideNavigation(
          _.onSelectionChange --> { event =>
            println(event.detail.item.id)
            val name = event.detail.item.id
            demos
              .find(_.name == name)
              .foreach(sampleVar.set)
          }
        )(demos.map(_.name).map(item))
      ),
      div(
        height := "100vh",
        overflowY := "auto",
        display := "flex",
        flexGrow := 1,
        div(
          padding := "10px",
          minWidth := "40%",
          maxWidth := "calc(100% - 320px)",
          table(
            tr(
              td(
                div(
                  child <-- sampleVar.signal.map(_.component)
                )
              ),
              td(
                div(
                  marginTop := "1em",
                  overflowX := "auto",
                  border := "0.0625rem solid #C1C1C1",
                  backgroundColor := "#f5f6fa",
                  padding := "1rem",
                  Title(_.level := "H3")("Code"),
                  child <-- sampleVar.signal
                    .map(_.source)
                    .map(src =>
                      pre(
                        code(
                          className := "language-scala",
                          src,
                          onMountCallback(ctx =>
                            hljs.highlightElement(ctx.thisNode.ref)
                          )
                        )
                      )
                    )
                )
              )
            ),
            tr(
              td(
                colSpan := 2,
                child <-- sampleVar.signal.map(_.debug)
              )
            )
          )
        )
      )
    )

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
}
