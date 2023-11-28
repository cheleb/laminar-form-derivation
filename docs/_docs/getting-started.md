# Getting Started

## Installation

```sbt
libraryDependencies += "dev.cheleb" %%% "laminar-form-derivation" % "{{ version }}"
```


## Sample

```scala sc:nocompile

import dev.cheleb.scalamigen.{*, given}
import dev.cheleb.scalamigen.ui5.UI5WidgetFactory

import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var

// Declare WidgetFactory for UI5 Web Components
given WidgetFactory = UI5WidgetFactory

// Declare model case class
case class Cat(name: String, age: Int)
case class Dog(name: String, age: Int)

case class EitherSample(
    either: Either[Cat, Dog],
    optionalInt: Option[Int]
)

// Laminar variable binding
val eitherVar = Var(EitherSample(Left(Cat("Scala le chat", 6)), Some(1)))

div(
  // Debug output of the model as soon as it changes.
  child <-- eitherVar.signal.map { item =>
    div(
      s"$item"
    )
  },
  // Render the forms
  Form.renderVar(eitherVar)
)


```