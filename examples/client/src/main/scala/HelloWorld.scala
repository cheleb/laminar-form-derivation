package demo

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import samples.tree

object App extends App {

  val sample = Var(samples.enums)

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
            case e @ Some("Either") =>
              sample.set(samples.either)
            case e @ Some("Enums") =>
              sample.set(samples.enums)
            case v @ Some("Person") =>
              sample.set(samples.person)
            case v @ Some("Validation") =>
              sample.set(samples.validation)
            case v @ Some("Lists") =>
              sample.set(
                samples.component
              )
            case v @ Some("Tree") =>
              sample.set(tree.component)
            case _ =>
              sample.set(div("????"))

          },
          Seq(
            SideNavigation.item(
              _.text := "Either",
              dataAttr("component-name") := "Either"
            ),
            SideNavigation.item(
              _.text := "Enums",
              dataAttr("component-name") := "Enums"
            ),
            SideNavigation.item(
              _.text := "Person",
              dataAttr("component-name") := "Person"
            ),
            SideNavigation.item(
              _.text := "Validation",
              dataAttr("component-name") := "Validation"
            ),
            SideNavigation.item(
              _.text := "List",
              dataAttr("component-name") := "Lists"
            ),
            SideNavigation.item(
              _.text := "Tree",
              dataAttr("component-name") := "Tree"
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

  val containerNode = dom.document.getElementById("app")
  render(containerNode, myApp)
}
