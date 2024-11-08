import uk.gov.hmrc.DefaultBuildSettings
import scoverage.ScoverageKeys

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("alcohol-duty-contact-preferences", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafmtOnCompile := true
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    ScoverageKeys.coverageExcludedFiles := scoverageExcludedList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 99,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
  )
  .settings(PlayKeys.playDefaultPort := 16006)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)

lazy val scoverageExcludedList: Seq[String] = Seq(
  "<empty>",
  "Reverse.*",
  ".*handlers.*",
  "uk.gov.hmrc.BuildInfo",
  "app.*",
  ".*SchemaValidationService.*",
  "prod.*",
  ".*Routes.*",
  "testOnly.*",
  ".*testOnly.*",
  ".*TestOnlyController.*",
  "testOnlyDoNotUseInAppConf.*",
  ".*config.*"
)

addCommandAlias("runAllChecks", ";clean;compile;scalafmtCheckAll;coverage;test;it/test;scalastyle;coverageReport")