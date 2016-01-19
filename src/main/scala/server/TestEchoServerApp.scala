/* Copyrights owned by Atos and Siemens, 2015. */

package server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.ws.{Message, UpgradeToWebsocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object TestEchoServerApp extends App {

  implicit val system = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val interval = 5000.millis
  implicit val timeout: Timeout = interval

  val identityFlow = Flow[Message]

  // asynchronous broken since 2.0.2
  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case req@HttpRequest(GET, Uri.Path("/my-client"), _, _, _) =>
      req.header[UpgradeToWebsocket] match {
        case Some(upgrade: UpgradeToWebsocket) => {
          Future(upgrade.handleMessages(identityFlow))
        }
        case None => Future(HttpResponse(404))
      }
  }

  val bindingFuture = Http().bindAndHandleAsync(requestHandler, "localhost", 8080)
}
