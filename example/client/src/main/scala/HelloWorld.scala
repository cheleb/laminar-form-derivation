package demo

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*

object App extends App {

  val sample = Var(samples.validation)

  val myApp =
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
          ] {
            case v @ Some("Person") =>
              sample.set(samples.person)
            case v @ Some("Validation") =>
              sample.set(samples.validation)
            case v @ Some(name) =>
              sample.set(
                div(
                  s"Hello $name"
                )
              )
            case None =>
              throw new IllegalArgumentException(
                s"This item did not have data 'componentName'."
              )
          },
          Seq(
            SideNavigation.item(
              _.text := "Person",
              dataAttr("component-name") := "Person"
            ),
            SideNavigation.item(
              _.text := "Validation",
              dataAttr("component-name") := "Validation"
            ),
            SideNavigation.item(
              _.text := "Item 3",
              dataAttr("component-name") := "item3"
            )
          )
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
          child <-- sample.signal
        )
      )
    )

  val containerNode = dom.document.getElementById("root")
  render(containerNode, myApp)
}
