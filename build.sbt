enablePlugins(JavaAppPackaging)

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "edu.duke.compsci516",
      scalaVersion := "2.13.4"
    )
  ),
  name := "IMDB",
  libraryDependencies ++= Dependencies.depends
)
