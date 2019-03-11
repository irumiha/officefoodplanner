// Makes our code tidy
addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

// Revolver allows us to use re-start and work a lot faster!
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Native Packager allows us to create standalone jar
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.17")

// Database migrations
addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.2.0")

// Twirl templates
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.4.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")

resolvers += "Flyway".at("https://davidmweber.github.io/flyway-sbt.repo")

resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"
