import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import spray.json._

import scala.concurrent.Future

object TweetApi extends App with TweetJsonProtocol with SprayJsonSupport {
  implicit val system = ActorSystem("TweetApi")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  implicit val timeout = Timeout(120 seconds)

  val config_tweet_ui = ConfigFactory.load("application.conf").getConfig("urls.tweet-ui")
  val url = config_tweet_ui.getString("url")
  val port = config_tweet_ui.getInt("port")

  val tweetValidator = system.actorOf(Props[TweetValidator], "tweetValidator")

  tweetValidator ! "just a try"

  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.POST, Uri.Path("/api/analysis"),_,entity,_) =>
      system.log.info("Got a Http Request with a content to analyze")
      val strictEntityFuture = entity.toStrict(3 seconds)
      strictEntityFuture.flatMap {
        strictEntity =>
          val tweetJsonString = strictEntity.data.utf8String
          val tweet = tweetJsonString.parseJson.convertTo[Tweet]
          val tweetMeasureFuture: Future[TweetMeasure] = (tweetValidator ? tweet).mapTo[TweetMeasure]

          tweetMeasureFuture.map {
            tweetMeasure =>
              HttpResponse(
                StatusCodes.OK,
                entity = HttpEntity(
                  ContentTypes.`application/json`,
                  tweetMeasure.toJson.prettyPrint
                )
            )
          }
      }
    case request: HttpRequest =>
      system.log.info("Got an incorrect message. No processing.")
      request.discardEntityBytes()
      Future(HttpResponse(StatusCodes.BadRequest))

  }

  Http().bindAndHandleAsync(asyncRequestHandler, url, port)


}
