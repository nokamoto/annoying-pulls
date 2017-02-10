scalaVersion := "2.11.8"

fork in run := true

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.12",
  "com.typesafe" % "config" % "1.3.1")
