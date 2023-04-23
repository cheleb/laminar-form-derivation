package dev.cheleb.scalamigen

import scala.annotation.StaticAnnotation

class EnumValues[A](val values: Array[A]) extends StaticAnnotation
