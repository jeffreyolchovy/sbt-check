package sbtcheck

object CheckKeys {
  val check = sbt.taskKey[CheckPlugin.Result]("Compile up to, and including, the typer phase")
}
