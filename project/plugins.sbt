// Makes our code tidy
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.2")

// Revolver allows us to use re-start and work a lot faster!
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Extract metadata from sbt and make it available to the code
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

// Native Packager allows us to create standalone jar
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25")

// Database migrations
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "6.0.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.2")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

resolvers += "jgit-repo" at "https://download.eclipse.org/jgit/maven"
