import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.3.0"
  private val hmrcMongoVersion    = "2.10.0"
  private val mockitoScalaVersion = "2.0.0"

  val compile = Seq(
    "org.typelevel"     %% "cats-core"                 % "2.13.0",
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.beachape"      %% "enumeratum"                % "1.9.0",
    "com.beachape"      %% "enumeratum-play"           % "1.9.0",
    "com.beachape"      %% "enumeratum-cats"           % "1.9.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test,
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0"
  )

  val itDependencies = Seq.empty
}
