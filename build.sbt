scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-target:jvm-1.8"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l", "slack.SlackTest", "-oDF")

fork in Test := true

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license"))

assemblyOutputPath in assembly := file("target/annoying-pulls-0.2.2-SNAPSHOT.jar")

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "io.netty.versions.properties" => MergeStrategy.discard
  case PathList("org", "apache", "commons", "logging", _ @ _*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val playVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % playVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.play" %% "play-netty-server" % playVersion % "test",
  "org.slf4j" % "slf4j-api" % "1.7.21",
  "ch.qos.logback" % "logback-classic" % "1.2.1")
