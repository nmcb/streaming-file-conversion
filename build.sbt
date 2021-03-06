import sbt.Keys._

lazy val `streaming-file-conversion` = (project in file("."))
  .settings(
    dependencies,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
  )


lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main
    "org.typelevel" %% "cats-core"   % "1.1.0",
    "org.typelevel" %% "cats-effect" % "0.10.1",
    "co.fs2"        %% "fs2-core"    % "0.10.4",
    "co.fs2"        %% "fs2-io"      % "0.10.4",
    // test
    "org.scalatest" %% "scalatest"   % "3.0.5" % "test"
  )
)

scalacOptions in ThisBuild ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification"
)

evictionWarningOptions in update := EvictionWarningOptions.empty
