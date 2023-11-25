# Getting Started

## Installation

```sbt
libraryDependencies += "dev.cheleb" %% "laminar-form-derivation" % "0.1.0"
```

### Requirements

### Sample

```scala sc:nocompile

import dev.cheleb.scalamigen.{*, given}
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

given WidgetFactory = UI5WidgetFactory

case class Cat(name: String, age: Int)
case class Dog(name: String, age: Int)

val either = {

  case class EitherSample(
      either: Either[Cat, Dog],
      optionalInt: Option[Int]
  )

  val eitherVar = Var(EitherSample(Left(Cat("Scala le chat", 6)), Some(1)))

  div(
    child <-- eitherVar.signal.map { item =>
      div(
        s"$item"
      )
    },
    Form.renderVar(eitherVar)
  )
}


```