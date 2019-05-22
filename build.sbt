import sbt.Tests.{Group, SubProcess}

val appName: String = "youtube-downloader"

val compile = Seq(
  "com.google.inject" % "guice" % "4.2.2",
  "com.softwaremill.sttp" %% "core" % "1.5.8"
)

def test(scope: String = "test, it"): Seq[ModuleID] = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % scope,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope
)

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq()

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 95,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] = tests map {
  test => Group(test.name, Seq(test), SubProcess(
    ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))
  ))
}

lazy val project: Project = Project(appName, file("."))
  .settings(coverageSettings: _*)
  .settings(
    scalaVersion := "2.12.8",
    scalaSource in Compile := baseDirectory.value / "app",
    scalaSource in Test := baseDirectory.value / "test",
    mainClass in run := Some((baseDirectory.value / "app.run.Main").toString),
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false
  )
