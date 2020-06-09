name := "Image manipulation project"

version := "1.0"

scalaVersion := "2.12.2"

resolvers += Resolver.typesafeRepo("releases")
libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.5.0",
	"com.typesafe.akka" %% "akka-http" % "10.1.5",
	"com.typesafe.akka" %% "akka-stream" % "2.5.17",
	"com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
	"io.spray" %%  "spray-json" % "1.3.4",
	"org.mongodb.scala" %% "mongo-scala-driver" % "2.4.2",
	"com.typesafe.play" %% "play-json" % "2.6.10",
	"com.pauldijou" %% "jwt-core" % "0.19.0",
	"ch.qos.logback" % "logback-classic" % "1.2.3",
	"com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",


	//image manipulation plugin
	"com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8",
	"com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8",
	"com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8"
)

Revolver.enableDebugging(port = 5005)
//javaOptions in reStart += "-jvm-debug 5005"
