# Usage

## Basic usage

```scala sc:nocompile
import dev.cheleb.scalamigen.*
```
This import is necessary to bring the implicit conversions into scope.

Now you can use the `asForm` method on any `Var` to render a form for the value it holds with double binding.

```scala sc:nocompile

case class Cat(name: String, age: Int)

val eitherVar = Var(Cat("Scala le chat", 6))

eitherVar.asForm  // (2) form rendering

```

As long as the case class is built on elments that have a `Form[..]` instance, the form will be rendered correctly.

## Supported types

* `String`, `Int`, `Long`, `Double`, `Boolean`
* `LocalDate`
* `Option[A]`
* `List[A]`
* `Either[A, B]`
* `IronType[T, C]`
  * `Positive`

## Customizing the form rendering

At anytime you can customize the form rendering by providing a `Form` instance for the type you want to render.

* `opaque type`
```scala sc:nocompile
opaque type Password = String
object Password:
  def apply(password: String): Password = password
  given Form[Password] = secretForm(apply) // (1) form instance

```
* `IronType`

```scala sc:nocompile
doubleGreaterThanEight: Double :| GreaterEqual[8.0]

given IronTypeValidator[Double, GreaterEqual[8.0]] =
    _.toDoubleOption match
      case None         => Left("Not a number")
      case Some(double) => double.refineEither[GreaterEqual[8.0]]

```
