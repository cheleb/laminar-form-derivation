package samples

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import facades.highlightjs.hljs
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory
import dev.cheleb.scalamigen.WidgetFactory

object DemoDoreane {

  def apply() =

    def item(name: String) = SideNavigation.item(
      _.text := name,
      dataAttr("component-name") := name
    )

    given WidgetFactory = UI5WidgetFactory

    val sampleVar = Var(samples.simple)

    val demos = App.demos

    div(
      display := "flex",
      div(
        paddingRight("2rem"),
        Title(
          "Demos",
          padding("0.5rem"),
          cursor := "pointer"
        ),
        SideNavigation(
          _.events.onSelectionChange
            .map(_.detail.item.dataset.get("componentName")) --> Observer[
            Option[String]
          ] { name =>
            name
              .flatMap(n => demos.find(_.name == n))
              .foreach(sampleVar.set)

          },
          demos.map(_.name).map(item)
        )
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
                  Title.h3("Code"),
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

}
