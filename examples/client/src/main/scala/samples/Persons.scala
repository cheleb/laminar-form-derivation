package samples

import dev.cheleb.scalamigen.{*, given}

import com.raquo.laminar.api.L.*
import magnolia1.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import samples.model.Password
import java.time.LocalDate

// Define some models
@Panel(true, "Person")
case class Person(
    @FieldName("First Name")
    name: String,
    password: Password,
    birthDate: LocalDate,
    fav: Pet,
    pet: Option[Pet],
    email: Option[String],
    age: BigInt,
    size: Double
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
    "Vlad",
    Password("not a password"),
    LocalDate.of(1431, 11, 8),
    Pet("Batman", 666, House(2), 169),
    Some(Pet("Wolfy", 12, House(1), 42)),
    Some("vlad.dracul@gmail.com"),
    48,
    1.85
  )

val personVar = Var(vlad)

val person = Sample(
  "Person",
  div(
    child <-- personVar.signal.map { item =>
      div(
        s"$item"
      )
    },
    personVar.asForm
  )
)
