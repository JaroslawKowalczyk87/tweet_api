case class Tweet(content: String)
case class TweetToMeasure(sentences: List[String], threshold: Double)
case class TweetMeasure(label: String, sentiment: SentimentMeasurement)
case class SentimentMeasurement(identityAttack: SentimentMeasurementComponent,
                                insult: SentimentMeasurementComponent,
                                obscene: SentimentMeasurementComponent,
                                severeToxicity: SentimentMeasurementComponent,
                                sexualExplicit: SentimentMeasurementComponent,
                                threat: SentimentMeasurementComponent,
                                toxicity: SentimentMeasurementComponent)
case class SentimentMeasurementComponent(`match`: Boolean, probability0: Double, probability1: Double)