package dev.cheleb.scalamigen

trait ConditionalFor[C, A]:
  def check: C => Boolean
