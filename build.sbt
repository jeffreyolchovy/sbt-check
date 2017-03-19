name := "sbt-check"

organization := "com.github.jeffreyolchovy"

licenses += ("BSD New", url("https://opensource.org/licenses/BSD-3-Clause"))

scalaVersion := "2.10.6"

scalacOptions ++= Seq("-deprecation", "-language:_")

sbtPlugin := true

scriptedSettings

scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

scriptedBufferLog := false

bintrayRepository := "sbt-plugins"
