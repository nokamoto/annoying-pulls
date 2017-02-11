scalaVersion := "2.11.8"

assemblyOutputPath in assembly := file("target/annoying-pulls-0.1.jar")

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "io.netty.versions.properties" => MergeStrategy.discard
  case PathList("org", "apache", "commons", "logging", ps @ _*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.12",
  "com.typesafe" % "config" % "1.3.1")
