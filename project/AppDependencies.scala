import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "9.11.0"
  private val hmrcMongoVersion    = "2.5.0"
  private val mockitoScalaVersion = "1.17.37"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"   % bootstrapVersion    % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % hmrcMongoVersion    % Test,
    "org.mockito"       %% "mockito-scala"            % mockitoScalaVersion % Test,
    "org.mockito"       %% "mockito-scala-scalatest"  % mockitoScalaVersion % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2"       % Test
  )

  val itDependencies = Seq.empty
}
