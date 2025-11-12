ThisBuild / scalaVersion := "3.7.4"
ThisBuild / version      := "0.1.0"

lazy val `streaming-file-conversion` =
  (project in file("."))
    .settings(dependencies)


lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main
    "org.typelevel" %% "cats-core"   % "2.13.0",
    "org.typelevel" %% "cats-effect" % "3.6.3",
    "co.fs2"        %% "fs2-core"    % "3.12.2",
    "co.fs2"        %% "fs2-io"      % "3.12.2",
    // test
    "org.scalatest" %% "scalatest"   % "3.2.19" % "test"
  )
)

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions"
)
