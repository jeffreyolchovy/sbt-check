package sbtcheck

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

trait AbstractCheckPlugin extends AutoPlugin {

  override def requires = JvmPlugin

  override def trigger = allRequirements

  object autoImport {
    val CheckKeys = sbtcheck.CheckKeys
    val check = CheckKeys.check
  }

  import autoImport._

  override def projectSettings = inTask(check)(scopedSettings) ++ Seq(
    check := (compile in check).value,
    scalacOptions in check += "-Ystop-after:typer"
  )

  def scopedSettings: Seq[Setting[_]]

  def exported(writer: java.io.PrintWriter, command: String): Seq[String] => Unit = {
    args => writer.println((command +: args).mkString(" "))
  }
}
