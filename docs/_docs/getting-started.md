# Getting Started

## Installation

```sbt
// With raw Laminar widgets (html only)
libraryDependencies += "dev.cheleb" %%% "laminar-form-derivation-ui" % "{{ projectVersion }}"
// With UI5 Web Components
libraryDependencies += "dev.cheleb" %%% "laminar-form-derivation-ui5" % "{{ projectVersion}}"
```

Annoations allow to customize the form rendering. They are part of the `laminar-form-derivation-shared` package.

```sbt
libraryDependencies += "dev.cheleb" %%% "laminar-form-derivation-shared" % "{{ projectVersion }}"
```

## Sample

```scala sc:nocompile
import com.raquo.laminar.api.L.*
import dev.cheleb.scalamigen.*

val eitherVar = Var(Cat("Scala le chat", 6))
div(
  child <-- eitherVar.signal.map { item =>
    div(
      s"$item"      // (1) debug case class
    )
  },
  eitherVar.asForm  // (2) form rendering
)
```

Will be rendered as:

![Sample Form](../images/simple-form.png)