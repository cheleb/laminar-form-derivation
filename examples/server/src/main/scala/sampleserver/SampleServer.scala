package sampleserver

import zio.*
import zio.http.*
//import zio.http.socket.{WebSocketChannelEvent, WebSocketFrame}

import zio.stream.ZStream
import zio.http.Path.Segment

//import zio.http.ChannelEvent.ChannelRead

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
    case Method.GET -> Root =>
      Http
        .fromResource("public/index.html")

    case Method.GET -> Root / "index.html" =>
      Http
        .fromResource("public/index.html")

    case Method.GET -> Root / "js" =>
      Http
        .fromResource("public/client-fastopt-bundle.js")
    case Method.GET -> "" /: "images" /: path =>
      Http
        .fromResource(s"public/images/$path")
    case Method.GET -> Root / "css" =>
      Http.fromResource("public/style.css")

    case Method.GET -> Root / "favicon.ico" =>
      Http.fromResource("public/favicon.ico")
  }

  val app = static

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
