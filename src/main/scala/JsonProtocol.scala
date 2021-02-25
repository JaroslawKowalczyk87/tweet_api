import spray.json.DefaultJsonProtocol

trait TweetJsonProtocol extends DefaultJsonProtocol {
  implicit val tweetFormat = jsonFormat1(Tweet)
  implicit val tweetToMeasureFormat = jsonFormat2(TweetToMeasure)
  implicit val sentimentMeasurementComponent = jsonFormat3(SentimentMeasurementComponent)
  implicit val sentimentMeasurementFormat = jsonFormat7(SentimentMeasurement)
  implicit val tweetMeasureFormat = jsonFormat2(TweetMeasure)
}