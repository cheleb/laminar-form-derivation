package samples

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import io.github.nguyenyou.ui5.webcomponents.laminar.*
import facades.highlightjs.{hljs, hljsScala}
import dev.cheleb.scalamigen.WidgetFactory

case class Sample(
    name: String,
    component: HtmlElement,
    debug: HtmlElement,
    source: String = "TODO"
)

object App extends App {
  hljs.registerLanguage("scala", hljsScala)

  def demos(using wf: WidgetFactory) = Seq(
    samples.simple,
    samples.opaque,
    samples.either,
    samples.validation,
    samples.conditional,
    samples.enums,
    samples.sealedClasses,
    samples.person,
    samples.list,
//    samples.tree,
    samples.adhoc
  )

  val demoVar = Var("ui5")

  val demo = div(
    SegmentedButton(
      _.accessibleName := "Map type",
      _.onSelectionChange --> { ev =>
        ev.detail.selectedItems.headOption
          .map(_.id)
          .foreach { id =>
            demoVar.set(id)
          }
      }
    )(
      SegmentedButtonItem(
        _.id := "native"
      )("Native"),
      SegmentedButtonItem(
        _.id := "ui5",
        _.selected := true
      )("UI5 Doreane"),
      SegmentedButtonItem(
        _.id := "ui5-nguyenyou"
      )("UI5 Nguyenyou"),
      SegmentedButtonItem(
        _.id := "webawesome"
      )("WebAwesome")
    ),
    child <-- demoVar.signal.map { id =>
      id match {
        case "native" =>
          div(
            h2("Native"),
            DemoNative()
          )
        case "ui5" =>
          div(
            h2("UI5 Doreane"),
            DemoDoreane()
          )
        case "ui5-nguyenyou" =>
          div(
            h2("UI5 Nguyenyou"),
            DemoNguyenyou()
          )
        case "webawesome" =>
          div(
            h2("WebAwesome"),
            DemoWebAwesome()
          )
      }
    }
  )
  val containerNode = dom.document.getElementById("app")
  render(containerNode, demo)
}
