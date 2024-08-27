package samples.model

import dev.cheleb.scalamigen.Form

import dev.cheleb.scalamigen.secretForm

opaque type Password = String

object Password:
  def apply(password: String): Password = password
  given Form[Password] = secretForm(apply)
