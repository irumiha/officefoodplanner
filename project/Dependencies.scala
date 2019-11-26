import sbt._

object Versions {
  val ScalaVersion              = "2.13.1"
  val CatsVersion               = "2.0.0"
  val CirceVersion              = "0.12.3"
  val CirceGenericExtrasVersion = "0.12.2"
  val CirceConfigVersion        = "0.7.0"
  val DoobieVersion             = "0.8.6"
  val EnumeratumVersion         = "1.5.13"
  val EnumeratumCirceVersion    = "1.5.22"
  val H2Version                 = "1.4.200"
  val Http4sVersion             = "0.21.0-M5"
  val LogbackVersion            = "1.2.3"
  val ScalaCheckVersion         = "1.14.2"
  val ScalaTestVersion          = "3.0.8"
  val FlywayVersion             = "6.0.8"
  val TsecVersion               = "0.2.0-M2"
  val ChimneyVersion            = "0.3.4"
  val OctopusVersion            = "0.4.0"
  val SeleniumVersion           = "2.53.0"
}

object Dependencies {
  import Versions._

  val scalaReflect       = "org.scala-lang"           %  "scala-reflect"            % ScalaVersion
  val doobieCore         = "org.tpolecat"             %% "doobie-core"              % DoobieVersion
  val doobieH2           = "org.tpolecat"             %% "doobie-h2"                % DoobieVersion
  val doobiePsql         = "org.tpolecat"             %% "doobie-postgres"          % DoobieVersion
  val doobiePsqlCirce    = "org.tpolecat"             %% "doobie-postgres-circe"    % DoobieVersion
  val doobieScalatest    = "org.tpolecat"             %% "doobie-scalatest"         % DoobieVersion
  val doobieHikari       = "org.tpolecat"             %% "doobie-hikari"            % DoobieVersion
  val catsCore           = "org.typelevel"            %% "cats-core"                % CatsVersion
  val circeGeneric       = "io.circe"                 %% "circe-generic"            % CirceVersion
  val virceLiteral       = "io.circe"                 %% "circe-literal"            % CirceVersion
  val circeGenericExtras = "io.circe"                 %% "circe-generic-extras"     % CirceGenericExtrasVersion
  val circeParser        = "io.circe"                 %% "circe-parser"             % CirceVersion
  val circeConfig        = "io.circe"                 %% "circe-config"             % CirceConfigVersion
  val enumeratum         = "com.beachape"             %% "enumeratum"               % EnumeratumVersion
  val enumeratumCirce    = "com.beachape"             %% "enumeratum-circe"         % EnumeratumCirceVersion
  val h2                 = "com.h2database"           %  "h2"                       % H2Version
  val flyway             = "org.flywaydb"             %  "flyway-core"              % FlywayVersion
  val http4sBlazeServer   = "org.http4s"              %% "http4s-blaze-server"      % Http4sVersion
  val http4sCirce         = "org.http4s"              %% "http4s-circe"             % Http4sVersion
  val http4sDsl           = "org.http4s"              %% "http4s-dsl"               % Http4sVersion
  val http4sahc           = "org.http4s"              %% "http4s-async-http-client" % Http4sVersion
  val logback             = "ch.qos.logback"          %  "logback-classic"          % LogbackVersion
  val chimney             = "io.scalaland"            %% "chimney"                  % ChimneyVersion
  val scalacheck          = "org.scalacheck"          %% "scalacheck"               % ScalaCheckVersion % Test
  val scalatest           = "org.scalatest"           %% "scalatest"                % ScalaTestVersion  % Test
  val tsecVersion         = "io.github.jmcardon"      %% "tsec-common"              % TsecVersion
  val tsecPassword        = "io.github.jmcardon"      %% "tsec-password"            % TsecVersion
  val tsecMac             = "io.github.jmcardon"      %% "tsec-mac"                 % TsecVersion
  val tsecSignatures      = "io.github.jmcardon"      %% "tsec-signatures"          % TsecVersion
  val tsecJwtMac          = "io.github.jmcardon"      %% "tsec-jwt-mac"             % TsecVersion
  val tsecJwtSig          = "io.github.jmcardon"      %% "tsec-jwt-sig"             % TsecVersion
  val tsecHttp4s          = "io.github.jmcardon"      %% "tsec-http4s"              % TsecVersion

  val seleniumJava        = "org.seleniumhq.selenium" % "selenium-java"             % SeleniumVersion % "test"
  val seleniumFirefox     = "org.seleniumhq.selenium" % "selenium-firefox-driver"   % SeleniumVersion % "test"

  val seleniumStack   = Seq(seleniumJava, seleniumFirefox)

}
