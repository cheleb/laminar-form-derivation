package dev.cheleb.scalamigen

trait Validator[A] {
  def isValid(a: A): Boolean
}
