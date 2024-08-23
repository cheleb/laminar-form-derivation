package samples.model

import dev.cheleb.scalamigen.Form
import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.WidgetFactory

opaque type Password = String

object Password:
  def apply(password: String): Password = password
  given Form[Password] with
    override def render(
        variable: Var[Password],
        syncParent: () => Unit,
        values: List[Password] = List.empty
    )(using factory: WidgetFactory): HtmlElement =
      factory.renderSecret
        .amend(
          value <-- variable.signal,
          onInput.mapToValue --> { v =>
            variable.set(v)
            syncParent()
          }
        )
