name := """fews-paper"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "joda-time" % "joda-time" % "2.7",
  "mysql" % "mysql-connector-java" % "5.1.35",
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "org.joda" % "joda-convert" % "1.7",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.0.0"
)

libraryDependencies += "de.l3s.boilerpipe" % "boilerpipe" % "1.1.0"
libraryDependencies += "org.jsoup" % "jsoup" % "1.8.2"
libraryDependencies += "net.sourceforge.nekohtml" % "nekohtml" % "1.9.22"
libraryDependencies += "xerces" % "xercesImpl" % "2.11.0"


resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
