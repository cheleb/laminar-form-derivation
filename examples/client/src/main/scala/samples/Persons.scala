package samples

import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*
import java.time.LocalDate

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

def person(using
    wf: WidgetFactory
): Sample = {
  // Define some models

  @NoPanel
  case class Person(
      @FieldName("First Name")
      name: String,
      password: Password,
      birthDate: LocalDate,
      fav: Pet,
      pet: Option[Pet],
      email: Option[String],
      age: BigInt,
      size: Double :| Positive
  )
  case class Pet(
      name: String,
      age: BigInt,
      House: House,
      size: Double :| Positive
  )

  case class House(capacity: Int)

  // Provide default for optional
  given Defaultable[Pet] with
    def default = Pet("No pet", 0, House(0), 1)

  // Instance your model
  val vlad =
    Person(
      "",
      Password("not a password"),
      LocalDate.of(1431, 11, 8),
      Pet("Batman", 666, House(2), 169),
      Some(Pet("Wolfy", 12, House(1), 42)),
      Some("vlad.dracul@gmail.com"),
      48,
      1.85
    )

  val personVar = Var(vlad)

  val errorBus = personVar.errorBus

  Sample(
    "Person",
    div(
      personVar.asForm(errorBus),
      div(
        child <-- errorBus.watch
          .map { errors =>
            div(
              errors.collect {
                case (field, ValidationStatus.Invalid(message, true)) =>
                  div(
                    s"$field: $message"
                  )
              }.toSeq
            )
          }
      )
    ),
    div(child <-- personVar.signal.map { item =>
      div(
        s"$item"
      )
    }),
    """|
           |  @NoPanel
           |  case class Person(
           |      @FieldName("First Name")
           |      name: String,
           |      password: Password,
           |      birthDate: LocalDate,
           |      fav: Pet,
           |      pet: Option[Pet],
           |      email: Option[String],
           |      age: BigInt,
           |      size: Double :| Positive
           |  )
           |  case class Pet(
           |      name: String,
           |      age: BigInt,
           |      House: House,
           |      size: Double :| Positive
           |  )
           |
           |  case class House(capacity: Int)
           |
           |  // Provide default for optional
           |  given Defaultable[Pet] with
           |    def default = Pet("No pet", 0, House(0), 1)
           |
           |  // Instance your model
           |  val vlad =
           |    Person(
           |      "",
           |      Password("not a password"),
           |      LocalDate.of(1431, 11, 8),
           |      Pet("Batman", 666, House(2), 169),
           |      Some(Pet("Wolfy", 12, House(1), 42)),
           |      Some("vlad.dracul@gmail.com"),
           |      48,
           |      1.85
           |    )
           |
           |""".stripMargin
  )
}
