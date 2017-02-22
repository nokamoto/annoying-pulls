scalaVersion := "2.11.8"

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

assemblyOutputPath in assembly := file("target/annoying-pulls-0.1.2-SNAPSHOT.jar")

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "io.netty.versions.properties" => MergeStrategy.discard
  case PathList("org", "apache", "commons", "logging", ps @ _*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "slack.SlackTest")

val playVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % playVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.play" %% "play-netty-server" % playVersion % "test")
