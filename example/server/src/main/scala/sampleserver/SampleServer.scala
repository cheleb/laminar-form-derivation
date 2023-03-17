package sampleserver

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

object SampleServer extends ZIOAppDefault {

  val static = Http.collectRoute[Request] {
    case Method.GET -> !! =>
      Http
        .fromResource("public/index.html")

    case Method.GET -> !! / "js" =>
      Http
        .fromResource("public/example-client-fastopt-bundle.js")
    case Method.GET -> "" /: "images" /: path =>
      Http
        .fromResource(s"public/images/$path")
    case Method.GET -> !! / "css" =>
      Http.fromResource("public/style.css")

    // case Method.GET -> "" /: "images" /: path =>
    //   Http.text(s"Path: $path")
    case Method.GET -> !! / "favicon.ico" =>
      Http.fromResource("public/favicon.ico")
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

  val app = dynamic ++ static ++ ws

  val config = ServerConfig.default
    .port(8888)
//    .leakDetection(LeakDetectionLevel.PARANOID)
//    .maxThreads(nThreads)
  val configLayer = ServerConfig.live(config)

  override val run =
    Server.serve(app.withDefaultErrorResponse).provide(configLayer, Server.live)
}
