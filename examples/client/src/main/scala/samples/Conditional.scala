package samples

import dev.cheleb.scalamigen.*
import com.raquo.laminar.api.L.*

case class Person(
    firstname: String,
    lastname: String,
    age: Int,
    extra_str: Option[ExtraString],
    extra_int: Option[ExtraInt]
)

object Person:
  val empty = Person("John", "Doe", 16, None, None)

@Panel("Conditional", false)
case class ConditionalSample(
    person: Person
)

given ConditionalFor[ConditionalSample, ExtraString] with
  def check = _.person.age >= 18

given ConditionalFor[ConditionalSample, ExtraInt] with
  def check = _.person.age >= 18

given Defaultable[Person] with
  def default = Person.empty

val conditionalVar = Var(ConditionalSample(Person.empty))

given formExtraString: Form[Option[ExtraString]] =
  Form.conditionalOn[ConditionalSample, ExtraString](conditionalVar)

given formExtraInt: Form[Option[ExtraInt]] =
  Form.conditionalOn[ConditionalSample, ExtraInt](conditionalVar)

def conditional(using
    wf: WidgetFactory
): Sample = {

  Sample(
    "Conditional",
    conditionalVar.asForm,
    div(child <-- conditionalVar.signal.map { item =>
      div(
        s"$item"
      )
    }),
    """|opaque type ExtraString = String
       |object ExtraString:
       |    def apply(s: String): ExtraString = s
       |    // given Form[ExtraString] = stringForm(identity)
       |    given Form[ExtraString] = stringFormWithValidation(using 
       |        new Validator[ExtraString] {
       |            override def validate(str: String): Either[String, ExtraString] = 
       |                str.matches("^[a-fA-F0-9]+$") match
       |                    case true => Right(str)
       |                    case false => Left("expected hexadecimal string (just for demo)")
       |        }
       |    )
       |    given Defaultable[ExtraString] with
       |        def default: ExtraString = ""
       |
       |opaque type ExtraInt = Int
       |object ExtraInt:
       |    def apply(i: Int): ExtraInt = i
       |    given Form[ExtraInt] = numericForm(_.toIntOption, 0)
       |    given Defaultable[ExtraInt] with
       |        def default: ExtraInt = 0
       |
       |// In another file
       |
       |case class Person(
       |    firstname: String, 
       |    lastname: String, 
       |    age: Int, 
       |    extra_str: Option[ExtraString],
       |    extra_int: Option[ExtraInt]
       |  )
       |
       |object Person:
       |    val empty = Person("John", "Doe", 16, None, None)
       |
       |@Panel("Conditional", false)
       |case class ConditionalSample(
       |    person: Person,
       |)
       |
       |given ConditionalFor[ConditionalSample, ExtraString] with
       |  def check = _.person.age >= 18
       |
       |given ConditionalFor[ConditionalSample, ExtraInt] with
       |  def check = _.person.age >= 18
       |
       |given Defaultable[Person] with
       |    def default = Person.empty
       |
       |val conditionalVar = Var(ConditionalSample(Person.empty))
       |
       |given formExtraString: Form[Option[ExtraString]] = 
       |    Form.conditionalOn[ConditionalSample, ExtraString](conditionalVar)
       |
       |given formExtraInt: Form[Option[ExtraInt]] = 
       |    Form.conditionalOn[ConditionalSample, ExtraInt](conditionalVar)
       |
       |conditionalVar.asForm
       |""".stripMargin
  )

}
