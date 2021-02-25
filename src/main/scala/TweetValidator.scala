import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}

import scala.util.{Failure, Success}
import spray.json._
import TweetApi.system
import TweetApi.materializer
import com.typesafe.config.ConfigFactory
import system.dispatcher

class TweetValidator extends Actor with ActorLogging with TweetJsonProtocol {

  val config_sentiment_analysis = ConfigFactory.load("application.conf").getConfig("urls.sentiment-analysis")
  val url = config_sentiment_analysis.getString("url")
  val port = config_sentiment_analysis.getString("port")

  override def receive: Receive = {
    case Tweet(content) =>
      log.info(s"Got a tweet with following content: $content")
      val forwardRequest = HttpRequest(
        HttpMethods.POST,
        uri = s"http://$url:$port/toxicity",
        entity = HttpEntity(
          ContentTypes.`application/json`,
          TweetToMeasure(List(content),0.9).toJson.prettyPrint
        )
      )

      log.info("Passing the message further for the sentiment analysis.")

      val responseFuture = Http().singleRequest(forwardRequest)

      responseFuture.onComplete {
        case Success(response) =>
          response.discardEntityBytes()
          log.info(s"The request was successful and returned: $response.")
          sender() ! response
        case Failure(ex) =>
          log.info(s"The request failed with: $ex.")
      }
    case _ =>
      log.info("Got a message with unknown format. Ignoring.")

  }
}