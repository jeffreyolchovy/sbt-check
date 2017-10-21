import sbtrelease.ReleaseStateTransformations._

name := "sbt-check"

organization := "com.github.jeffreyolchovy"

licenses += ("BSD New", url("https://opensource.org/licenses/BSD-3-Clause"))

scalacOptions ++= Seq("-deprecation", "-language:_")

sbtPlugin := true

crossSbtVersions := Seq("0.13.16", "1.0.1")

scriptedSettings

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

scriptedBufferLog := false

sbtTestDirectory := {
  val currentSbtVersion = (sbtVersion in pluginCrossBuild).value
  CrossVersion.partialVersion(currentSbtVersion) match {
    case Some((0, 13)) => sourceDirectory.value / "sbt-test-0.13"
    case Some((1, _))  => sourceDirectory.value / "sbt-test-1.0"
    case _             => sys.error(s"Unsupported sbt version: $currentSbtVersion")
  }
}

bintrayRepository := "sbt-plugins"

releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  setReleaseVersion,
  releaseStepCommandAndRemaining("^clean"),
  releaseStepCommandAndRemaining("^scripted"),
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^publish"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
