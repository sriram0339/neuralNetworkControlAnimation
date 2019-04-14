name := "HSCCDemo"

version := "0.1"

scalaVersion := "2.12.8"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"


libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-swing" % "2.0.3",
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "com.github.fons" %% "nr" % "1.0" from  "file://./jars/scala-2.12/numrecip_2.12-1.0.jar"
)

