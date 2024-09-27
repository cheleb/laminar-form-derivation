package dev.cheleb.scalamigen

import com.raquo.airstream.state.Var

extension [A](v: Var[A])
  def asForm(using WidgetFactory, Form[A]) = Form.renderVar(v)
