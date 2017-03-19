package sbtcheck

import sbt._, Keys._, Defaults._
import sbt.CommandStrings.ExportStream
import sbt.Compiler.InputsWithPrevious
import sbt.compiler.MixedAnalyzingCompiler
import sbt.inc.Analysis
import sbt.plugins.JvmPlugin

object CheckKeys {
  val check = taskKey[Analysis]("Compile up to, and including, the typer phase")
}

object CheckPlugin extends AutoPlugin {

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

  lazy val scopedSettings = Seq(
    compile := compileTask.value,
    manipulateBytecode := compileIncremental.value,
    compileIncremental := (compileIncrementalTask tag (Tags.Compile, Tags.CPU)).value,
    compileIncSetup := (compileIncSetup in Compile).value,
    compileInputs := {
      val cp = (classDirectory in Compile).value +: Attributed.data((dependencyClasspath in Compile).value)
      Compiler.inputs(
        cp,
        (sources in Compile).value,
        (classDirectory in Compile).value,
        (scalacOptions in check).value,
        javacOptions.value,
        maxErrors.value,
        sourcePositionMappers.value,
        compileOrder.value
      )(compilers.value, (compileIncSetup in check).value, streams.value.log)
    },
    previousCompile := {
      val setup: Compiler.IncSetup = (compileIncSetup in check).value
      val store = MixedAnalyzingCompiler.staticCachedStore(setup.cacheFile)
      store.get() match {
        case Some((an, setup)) => Compiler.PreviousAnalysis(an, Some(setup))
        case None              => Compiler.PreviousAnalysis(Analysis.Empty, None)
      }
    }
  )

  def compileIncrementalTask = Def.task {
    compileIncrementalTaskImpl(
      streams.value,
      (compileInputs in check).value,
      (previousCompile in check).value
    )
  }

  def compileIncrementalTaskImpl(
    s: TaskStreams,
    ci: Compiler.Inputs,
    previous: Compiler.PreviousAnalysis
  ): Compiler.CompileResult = {
    lazy val x = s.text(ExportStream)
    def onArgs(cs: Compiler.Compilers) = cs.copy(
      scalac = cs.scalac.onArgs(exported(x, "scalac")),
      javac = cs.javac.onArgs(exported(x, "javac"))
    )
    val i = InputsWithPrevious(ci.copy(compilers = onArgs(ci.compilers)), previous)
    try {
      Compiler.compile(i, s.log)
    } finally x.close() // workaround for #937
  }

  def exported(w: java.io.PrintWriter, command: String): Seq[String] => Unit = {
    args => w.println((command +: args).mkString(" "))
  }
}
