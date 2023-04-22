package samples

import dev.cheleb.scalamigen.Defaultable

case class Cat(name: String, weight: Int)
case class Dog(name: String, weight: Int)

given Defaultable[Cat] with
  def default = Cat("", 0)

given Defaultable[Dog] with
  def default = Dog("", 0)
