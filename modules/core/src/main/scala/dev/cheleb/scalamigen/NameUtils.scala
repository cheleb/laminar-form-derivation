package dev.cheleb.scalamigen

object NameUtils {

  /** someParameterName -> Some Parameter Name camelCase -> Title Case
    */
  def titleCase(string: String): String =
    string
      .filter(_.isLetter)
      .split("(?=[A-Z])")
      .map(_.capitalize)
      .mkString(" ")
}
