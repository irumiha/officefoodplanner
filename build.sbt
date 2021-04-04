import sbt._
import Keys._
import scala.sys.process.Process
import complete.DefaultParsers._

import Dependencies._
import CompilerOptions._

lazy val updateNpm  = taskKey[Unit]("Update npm")
lazy val npmTask    = inputKey[Unit]("Run npm with arguments")
lazy val distApp    = taskKey[Unit]("Build final app package")

ThisBuild / organization := "com.officefoodplanner"
ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := Versions.ScalaVersion
ThisBuild / resolvers    += Resolver.sonatypeRepo("releases")

ThisBuild / updateNpm := {
  println("Updating npm dependencies")
  haltOnCmdResultError(Process("npm install", (rootProject / baseDirectory).value / "ui").!)
}

ThisBuild / npmTask := {
  val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
  updateNpm.value
  val localNpmCommand = "npm " + taskName
  def buildWebpack() =
    Process(localNpmCommand, (rootProject / baseDirectory).value / "ui").!
  println("Building with Webpack : " + taskName)
  haltOnCmdResultError(buildWebpack())
}

// ThisBuild / turbo := true

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

lazy val rootProject = (project in file("."))
  .settings(
    name := "officefoodplanner",
//    herokuFatJar in Compile := Some((assemblyOutputPath in backend in assembly).value),
//    deployHeroku in Compile := ((deployHeroku in Compile) dependsOn (assembly in backend)).value
  )
  .aggregate(persistence, backend, ui)

lazy val persistence = (project in file("persistence"))
  .settings(
    name := "officefoodplanner-persistence",
    libraryDependencies ++= Seq(
      scalaReflect,
      doobieCore,
      doobieH2,
      doobiePsql,
      doobiePsqlCirce,
      doobieScalatest,
      doobieHikari,
      catsCore,
      circeGeneric,
      virceLiteral,
      circeGenericExtras,
      circeParser,
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full),
    addCompilerPlugin("org.augustjune" %% "context-applied" % "0.1.4")
  )

lazy val ui = (project in file("ui"))
  .settings(Test / test := (Test / test).dependsOn(npmTask.toTask(" run test")).value)

lazy val uiTests = (project in file("ui-tests"))
  .settings(
    parallelExecution := false,
    fork := true,
    libraryDependencies ++= seleniumStack,
    Test / test := (Test / test).dependsOn(npmTask.toTask(" run build")).value
  ) dependsOn backend

// Filter out compiler flags to make the repl experience functional...
val badConsoleFlags = Seq("-Xfatal-warnings", "-Ywarn-unused:imports")

lazy val backend = (project in file("backend"))
  .enablePlugins(ScalafmtPlugin, JavaServerAppPackaging, SystemdPlugin, FlywayPlugin)
  .settings(
    name := "officefoodplanner-backend"
  )
  .settings(
    libraryDependencies ++= Seq(
      scalaReflect,
      doobieCore,
      doobieH2,
      doobiePsql,
      doobiePsqlCirce,
      doobieScalatest,
      doobieHikari,
      catsCore,
      circeGeneric,
      virceLiteral,
      circeGenericExtras,
      circeParser,
      circeConfig,
      enumeratum,
      enumeratumCirce,
      h2,
      flyway,
      http4sBlazeServer,
      http4sCirce,
      http4sDsl,
      http4sahc,
      logback,
      chimney,
      scalacheck,
      scalatest,
      scalatestPlus,
      tsecVersion,
      tsecPassword,
      tsecMac,
      tsecSignatures,
      tsecJwtMac,
      tsecJwtSig,
      tsecHttp4s,
    ),
    scalacOptions ++= compilerOptions,
    flywayUrl := "jdbc:postgresql://localhost:5432/officefoodplanner",
    flywayUser := "officefoodplanner",
    flywayPassword := "officefoodplanner",
    flywayLocations += "db/migration",
    Test / flywayUrl := "jdbc:postgresql://localhost:5432/officefoodplanner",
    Test / flywayUser := "officefoodplanner",
    Test / flywayPassword := "officefoodplanner",
    Compile / console / scalacOptions ~= (_.filterNot(badConsoleFlags.contains(_))),
    // Support stopping the running server
    reStart / mainClass := Some("com.officefoodplanner.ApplicationServer"),
    fork := true,
    Compile / unmanagedResourceDirectories := {
      (Compile / unmanagedResourceDirectories).value ++ List(
        baseDirectory.value.getParentFile / ui.base.getName / "dist"
      )
    },
    Compile / compile := {
      val compilationResult = (Compile / compile).value
      IO.touch(target.value / "compilationFinished")

      compilationResult
    },
    distApp := dist.dependsOn((ui / npmTask).toTask(" run build")).value,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.3" cross CrossVersion.full),
    addCompilerPlugin("org.augustjune" %% "context-applied" % "0.1.4")
  ) dependsOn persistence
