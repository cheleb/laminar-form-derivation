package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

def adhoc(using
    wf: WidgetFactory
): Sample = {

  // outside your scope / in another library
  // => meaning '@FieldName' or '@NoPanel' annotations are not possible
  case class Cat(
      name: String,
      kind: Boolean = true,
      color: Color
  )

  enum Color(val code: String):
    case Black extends Color("000")
    case White extends Color("FFF")
    case Isabelle extends Color("???")

  // your library
  case class Basket(color: Color, cat: Cat)

  given colorForm: Form[Color] =
    selectForm(Color.values, labelMapper = c => s"$c ${c.code}")
      .withFieldName("Select color")

  given Form[Cat] =
    Form
      .autoDerived[Cat]
      .withFieldName("Your cat")
      .withPanelConfig(label = Some("What kind of cat ?"), asTable = true)

  val enumVar = Var(
    Basket(
      Color.Black,
      Cat(
        "Scala",
        true,
        Color.White
      )
    )
  )

  Sample(
    "Ad-Hoc",
    enumVar.asForm(enumVar.errorBus),
    div(
      child <-- enumVar.signal.map { item =>
        div(
          s"$item"
        )
      }
    ),
    """|
       |// outside your scope / in another library
       |// => meaning '@FieldName' or '@NoPanel' annotations are not possible
       |case class Cat(
       |    name: String,
       |    kind: Boolean = true,
       |    color: Color,
       |)
       |
       |enum Color(val code: String):
       |  case Black extends Color("000")
       |  case White extends Color("FFF")
       |  case Isabelle extends Color("???")
       |
       |// your library
       |case class Basket(color: Color, cat: Cat)
       |
       |given colorForm: Form[Color] =
       |  selectForm(Color.values, labelMapper = c => s"$c ${c.code}")
       |    .withFieldName("Select color")
       |
       |@annotation.nowarn
       |given Form[Cat] = 
       |  Form.autoDerived[Cat]
       |      .withFieldName("Your cat")
       |      .withPanelConfig(label = Some("What kind of cat ?"), asTable = true)
       |""".stripMargin
  )
}
