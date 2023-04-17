package sampleserver

import zio.*
import zio.http.*
import zio.http.socket.{WebSocketChannelEvent, WebSocketFrame}

import zio.stream.ZStream
import zio.http.Path.Segment
import zio.http.Path.Segment.Root
import zio.http.ChannelEvent.ChannelRead

object HH:
  def contentType(path: Path): Option[Header] =
    path.lastSegment
      .map(p => p.text.substring(p.text.lastIndexOf('.') + 1))
      .flatMap(ext =>
        MediaType
          .forContentType(s"image/$ext")
      )
      .map(ext => Header.ContentType(ext))

object SampleServer extends ZIOAppDefault {

  val static = Http.collectHttp[Request] {
    case Method.GET -> !! =>
      Http
        .fromResource("public/index.html")

    case Method.GET -> !! / "index.html" =>
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

  val port = sys.env.get("PORT").map(_.toInt).getOrElse(8888)

  val config = Server.Config.default
    .port(port)
//    .leakDetection(LeakDetectionLevel.PARANOID)
//    .maxThreads(nThreads)
  val configLayer = ZLayer.succeed(config)

  override val run =
    ZIO.debug(s"Starting server: http://localhost:$port/index.html") *>
      Server
        .serve(app.withDefaultErrorResponse)
        .provide(configLayer, Server.live)
}
