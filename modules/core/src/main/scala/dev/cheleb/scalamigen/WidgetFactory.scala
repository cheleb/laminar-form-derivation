package dev.cheleb.scalamigen

import com.raquo.laminar.api.L.HtmlElement
import com.raquo.laminar.modifiers.EventListener

trait WidgetFactory:
  def renderText: HtmlElement
  def renderLabel(required: Boolean, name: String): HtmlElement
  def renderNumeric: HtmlElement
  def renderButton: HtmlElement
  def renderLink(text: String, obs: EventListener[_, _]): HtmlElement
  def renderPanel(headerText: String): HtmlElement
  def renderUL(id: String): HtmlElement
  def renderSelect(f: Int => Unit): HtmlElement
  def renderOption(label: String, idx: Int, selected: Boolean): HtmlElement
