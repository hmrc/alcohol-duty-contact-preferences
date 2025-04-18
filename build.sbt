import uk.gov.hmrc.DefaultBuildSettings
import scoverage.ScoverageKeys

val playVersion = "play-30"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / excludeDependencies ++= Seq(
  // As of Play 3.0, groupId has changed to org.playframework; exclude transitive dependencies to the old artifacts
  // Specifically affects play-json-extensions dependency
  ExclusionRule(organization = "com.typesafe.play")
)


lazy val microservice = Project("alcohol-duty-contact-preferences", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencies += "uk.gov.hmrc" %% s"crypto-json-$playVersion" % "8.2.0",
      // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalafmtOnCompile := true
  )
  .settings(inConfig(Test)(testSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    ScoverageKeys.coverageExcludedFiles := scoverageExcludedList.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
  .settings(PlayKeys.playDefaultPort := 16006)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
  .settings(
    Test / parallelExecution := false,
    Test / fork := true,
    Test / scalafmtOnCompile := true
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

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

addCommandAlias("runAllChecks", ";clean;test:compile;scalafmtCheckAll;coverage;test;it/test;scalastyle;coverageReport")
