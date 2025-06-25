import sbt.Keys._

lazy val `streaming-file-conversion` = (project in file("."))
  .settings(
    dependencies,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.10")
  )


lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    // main
    "org.typelevel" %% "cats-core"   % "1.6.1",
    "org.typelevel" %% "cats-effect" % "3.6.1",
    "co.fs2"        %% "fs2-core"    % "0.10.4",
    "co.fs2"        %% "fs2-io"      % "0.10.4",
    // test
    "org.scalatest" %% "scalatest"   % "3.0.5" % "test"
  )
)

(ThisBuild / scalacOptions) ++= Seq(
  "-unchecked",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ypartial-unification"
)

(update / evictionWarningOptions) := EvictionWarningOptions.empty
