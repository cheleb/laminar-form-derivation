package polyserver

import zio.*
import zio.http.*
import zio.http.socket.{WebSocketChannelEvent, WebSocketFrame}

import zio.http.model.Method
import zio.stream.ZStream
import zio.http.model.Headers
import zio.http.Path.Segment
import zio.http.Path.Segment.Root
import zio.http.ChannelEvent.ChannelRead

object HH:
  def contentType(path: Path): Headers =
    path.lastSegment
      .map(p => p.text.substring(p.text.lastIndexOf('.') + 1))
      .map(ext => Headers.contentType(s"image/$ext"))
      .getOrElse(Headers.empty)

object PolyServer extends ZIOAppDefault {

  val static = Http.collectHttp[Request] {
    case Method.GET -> !! =>
      Http
        .fromStream(ZStream.fromResource("public/index.html"))
        .addHeaders(Headers.contentType("text/html"))
    case Method.GET -> !! / "js" =>
      Http
        .fromStream(
          ZStream.fromResource("public/example-client-fastopt-bundle.js")
        )
        .addHeaders(Headers.contentType("application/javascript"))
    case Method.GET -> "" /: "images" /: path =>
      Http
        .fromStream(ZStream.fromResource(s"public/images/$path"))
        .addHeaders(HH.contentType(path))
    case Method.GET -> !! / "css" =>
      Http.fromStream(ZStream.fromResource("public/style.css"))

    case Method.GET -> "" /: "images" /: path =>
      Http.text(s"Path: $path")
    case Method.GET -> !! / "favicon.ico" =>
      Http.fromStream(ZStream.fromResource("public/favicon.ico"))
  }

  private val socket =
    Http.collectZIO[WebSocketChannelEvent] {
      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Ping)) =>
        ch.writeAndFlush(WebSocketFrame.Pong)

      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Pong)) =>
        ch.writeAndFlush(WebSocketFrame.Ping)

      case ChannelEvent(ch, ChannelRead(WebSocketFrame.Text(text))) =>
        ch.write(WebSocketFrame.text(text)).repeatN(10) *> ch.flush
    }

  val ws = Http.collectZIO[Request] {
    case Method.GET -> !! / "greet" / name =>
      ZIO.succeed(Response.text(s"Greetings {$name}!"))
    case Method.GET -> !! / "subscriptions" =>
      socket.toSocketApp.toResponse
  }

  val dynamic = Http.collectZIO[Request] {
    case Method.GET -> "" /: "hello" /: name =>
      ZIO.succeed(Response.text(s"Hello, $name!"))
  }

  val app: HttpApp[Any, Nothing] = dynamic ++ static ++ ws

  override val run =
    Server.serve(app).provide(Server.default)
}
