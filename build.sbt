import sbt._
import Keys._
import scala.sys.process.Process
import complete.DefaultParsers._

lazy val updateNpm  = taskKey[Unit]("Update npm")
lazy val npmTask    = inputKey[Unit]("Run npm with arguments")
lazy val distApp    = taskKey[Unit]("Build final app package")

// ThisBuild / turbo := true

def haltOnCmdResultError(result: Int) {
  if (result != 0) {
    throw new Exception("Build failed.")
  }
}

lazy val commonSettings = Seq(
  organization := "com.officefoodplanner",
  version      := "0.0.1-SNAPSHOT",
  scalaVersion := ScalaVersion,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  updateNpm := {
    println("Updating npm dependencies")
    haltOnCmdResultError(Process("npm install", baseDirectory.value / ".." / "ui").!)
  },
  npmTask := {
    val taskName = spaceDelimited("<arg>").parsed.mkString(" ")
    updateNpm.value
    val localNpmCommand = "npm " + taskName
    def buildWebpack() =
      Process(localNpmCommand, baseDirectory.value / ".." / "ui").!
    println("Building with Webpack : " + taskName)
    haltOnCmdResultError(buildWebpack())
  },
  resolvers += Resolver.sonatypeRepo("releases")
)

resolvers += Resolver.sonatypeRepo("snapshots")

val ScalaVersion           = "2.13.0"
val CatsVersion            = "2.0.0"
val CirceVersion           = "0.12.1"
val CirceConfigVersion     = "0.7.0-M1"
val DoobieVersion          = "0.8.0-RC1"
val EnumeratumVersion      = "1.5.13"
val EnumeratumCirceVersion = "1.5.21"
val H2Version              = "1.4.199"
val Http4sVersion          = "0.21.0-M4"
val LogbackVersion         = "1.2.3"
val ScalaCheckVersion      = "1.14.0"
val ScalaTestVersion       = "3.0.8"
val FlywayVersion          = "6.0.2"
val TsecVersion            = "0.2.0-M1"
val ChimneyVersion         = "0.3.2"
val OctopusVersion         = "0.4.0"
val SeleniumVersion        = "2.53.0"

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "officefoodplanner",
//    herokuFatJar in Compile := Some((assemblyOutputPath in backend in assembly).value),
//    deployHeroku in Compile := ((deployHeroku in Compile) dependsOn (assembly in backend)).value
  )
  .aggregate(persistence, backend, ui)

lazy val persistence = (project in file("persistence"))
  .settings(commonSettings: _*)
  .settings(
    name := "officefoodplanner-persistence",
    libraryDependencies ++= Seq(
      "org.scala-lang"        %  "scala-reflect"            % ScalaVersion,
      "org.tpolecat"          %% "doobie-core"              % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"                % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"          % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres-circe"    % DoobieVersion,
      "org.tpolecat"          %% "doobie-scalatest"         % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"            % DoobieVersion,
      "org.typelevel"         %% "cats-core"                % CatsVersion,
      "io.circe"              %% "circe-generic"            % CirceVersion,
      "io.circe"              %% "circe-literal"            % CirceVersion,
      "io.circe"              %% "circe-generic-extras"     % CirceVersion,
      "io.circe"              %% "circe-parser"             % CirceVersion,
    ),
  )

lazy val ui = (project in file("ui"))
  .settings(commonSettings: _*)
  .settings(test in Test := (test in Test).dependsOn(npmTask.toTask(" run test")).value)

val seleniumJava    = "org.seleniumhq.selenium" % "selenium-java" % SeleniumVersion % "test"
val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % SeleniumVersion % "test"
val seleniumStack   = Seq(seleniumJava, seleniumFirefox)

lazy val uiTests = (project in file("ui-tests"))
  .settings(commonSettings: _*)
  .settings(
    parallelExecution := false,
    fork := true,
    libraryDependencies ++= seleniumStack,
    test in Test := (test in Test).dependsOn(npmTask.toTask(" run build")).value
  ) dependsOn backend

// Filter out compiler flags to make the repl experience functional...
val badConsoleFlags = Seq("-Xfatal-warnings", "-Ywarn-unused:imports")

lazy val backend = (project in file("backend"))
  .enablePlugins(ScalafmtPlugin, JavaServerAppPackaging, FlywayPlugin)
  .settings(
    name := "officefoodplanner-backend"
  )
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      // FP goodness
      "org.typelevel"         %% "cats-core"                % CatsVersion,
      // Json serialization/deserialization
      "io.circe"              %% "circe-generic"            % CirceVersion,
      "io.circe"              %% "circe-literal"            % CirceVersion,
      "io.circe"              %% "circe-generic-extras"     % CirceVersion,
      "io.circe"              %% "circe-parser"             % CirceVersion,
      "io.circe"              %% "circe-config"             % CirceConfigVersion,
      // Database access
      "org.tpolecat"          %% "doobie-core"              % DoobieVersion,
      "org.tpolecat"          %% "doobie-h2"                % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres"          % DoobieVersion,
      "org.tpolecat"          %% "doobie-postgres-circe"    % DoobieVersion,
      "org.tpolecat"          %% "doobie-scalatest"         % DoobieVersion,
      "org.tpolecat"          %% "doobie-hikari"            % DoobieVersion,
      "com.beachape"          %% "enumeratum"               % EnumeratumVersion,
      "com.beachape"          %% "enumeratum-circe"         % EnumeratumCirceVersion,
      "com.h2database"        %  "h2"                       % H2Version,
      "org.flywaydb"          %  "flyway-core"              % FlywayVersion,
      // HTTP server
      "org.http4s"            %% "http4s-blaze-server"      % Http4sVersion,
      "org.http4s"            %% "http4s-circe"             % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"               % Http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"      % Http4sVersion,
      "org.http4s"            %% "http4s-async-http-client" % Http4sVersion,
      // Logging
      "ch.qos.logback"        %  "logback-classic"          % LogbackVersion,
      // Automatic DTO mapping
      "io.scalaland"          %% "chimney"                  % ChimneyVersion,
      // Test deps
      "org.scalacheck"        %% "scalacheck"               % ScalaCheckVersion % Test,
      "org.scalatest"         %% "scalatest"                % ScalaTestVersion  % Test,
      // Authentication dependencies
      "io.github.jmcardon"    %% "tsec-common"              % TsecVersion,
      "io.github.jmcardon"    %% "tsec-password"            % TsecVersion,
      "io.github.jmcardon"    %% "tsec-mac"                 % TsecVersion,
      "io.github.jmcardon"    %% "tsec-signatures"          % TsecVersion,
      "io.github.jmcardon"    %% "tsec-jwt-mac"             % TsecVersion,
      "io.github.jmcardon"    %% "tsec-jwt-sig"             % TsecVersion,
      "io.github.jmcardon"    %% "tsec-http4s"              % TsecVersion,
    ),
    scalacOptions ++= Seq(
      // format: off
      "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8",                // Specify character encoding used by source files.
      "-explaintypes",                     // Explain type errors in more detail.
      "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
      "-language:higherKinds",             // Allow higher-kinded types
      "-language:implicitConversions",     // Allow definition of implicit functions called views
      "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
      //  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code",                  // Warn when dead code is identified.
      "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen",              // Warn when numerics are widened.
      "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",              // Warn if a local definition is unused.
      "-Ywarn-unused:params",              // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",            // Warn if a private member is unused.
      "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
      // format: on
    ),
    flywayUrl := "jdbc:postgresql://localhost:5432/officefoodplanner",
    flywayUser := "officefoodplanner",
    flywayPassword := "officefoodplanner",
    flywayLocations += "db/migration",
    flywayUrl in Test := "jdbc:postgresql://localhost:5432/officefoodplanner",
    flywayUser in Test := "officefoodplanner",
    flywayPassword in Test := "officefoodplanner",
    scalacOptions in (Compile, console) ~= (_.filterNot(badConsoleFlags.contains(_))),
    // Support stopping the running server
    mainClass in reStart := Some("com.officefoodplanner.ApplicationServer"),
    fork := true,
    unmanagedResourceDirectories in Compile := {
      (unmanagedResourceDirectories in Compile).value ++ List(
        baseDirectory.value.getParentFile / ui.base.getName / "dist"
      )
    },
    compile in Compile := {
      val compilationResult = (compile in Compile).value
      IO.touch(target.value / "compilationFinished")

      compilationResult
    },
    distApp := dist.dependsOn((npmTask in ui).toTask(" run build")).value
  ) dependsOn persistence
