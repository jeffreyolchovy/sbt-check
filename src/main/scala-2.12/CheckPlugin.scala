package sbtcheck

import sbt._, Keys._, Defaults._
import sbt.internal.CommandStrings.ExportStream
import sbt.internal.inc.{
  AnalyzingCompiler,
  MixedAnalyzingCompiler,
  ZincUtil
}
import sbt.util.InterfaceUtil.toJavaFunction
import xsbti.compile.{
  Compilers,
  CompileAnalysis,
  CompileOptions,
  CompileResult,
  Inputs,
  MiniSetup,
  PreviousResult
}

object CheckPlugin extends AbstractCheckPlugin {

  import autoImport._

  type Result = CompileAnalysis

  private val incCompiler = ZincUtil.defaultIncrementalCompiler

  def scopedSettings = Seq(
    compile := compileTask.value,
    manipulateBytecode := compileIncremental.value,
    compileIncremental := (compileIncrementalTask tag (Tags.Compile, Tags.CPU)).value,
    compileIncSetup := (compileIncSetup in Compile).value,
    compileOptions := CompileOptions.of(
      ((classDirectory in Compile).value +: Attributed.data((dependencyClasspath in Compile).value)).toArray,
      (sources in Compile).value.toArray,
      (classDirectory in Compile).value,
      (scalacOptions in check).value.toArray,
      javacOptions.value.toArray,
      maxErrors.value,
      toJavaFunction(foldMappers(sourcePositionMappers.value)),
      compileOrder.value
    ),
    compileInputs := Inputs.of(
      compilers.value,
      (compileOptions in check).value,
      (compileIncSetup in check).value,
      (previousCompile in check).value
    ),
    previousCompile := {
      val setup = (compileIncSetup in check).value
      val useBinary: Boolean = enableBinaryCompileAnalysis.value
      val store = MixedAnalyzingCompiler.staticCachedStore(setup.cacheFile, !useBinary)
      store.get().toOption match {
        case Some(contents) =>
          val analysis = Option(contents.getAnalysis).toOptional
          val setup = Option(contents.getMiniSetup).toOptional
          PreviousResult.of(analysis, setup)
        case None => PreviousResult.of(jnone[CompileAnalysis], jnone[MiniSetup])
      }
    }
  )

  def compileIncrementalTask = Def.task {
    compileIncrementalTaskImpl(
      streams.value,
      (compileInputs in check).value
    )
  }

  def compileIncrementalTaskImpl(
    s: TaskStreams,
    ci: Inputs
  ): CompileResult = {
    lazy val x = s.text(ExportStream)
    def onArgs(cs: Compilers) = cs.withScalac(
      cs.scalac match {
        case ac: AnalyzingCompiler => ac.onArgs(exported(x, "scalac"))
        case x => x
      }
    )
    val compilers: Compilers = ci.compilers
    val i = ci.withCompilers(onArgs(compilers))
    try {
      incCompiler.compile(i, s.log)
    } finally x.close() // workaround for #937
  }

  def foldMappers[A](mappers: Seq[A => Option[A]]) = {
    mappers.foldRight({
      (p: A) => p 
    }) { (mapper, mappers) =>
      (p: A) => mapper(p).getOrElse(mappers(p))
    }
  }

  def none[A]: Option[A] = (None: Option[A])

  def jnone[A]: java.util.Optional[A] = none[A].toOptional

  implicit class RichOptional[A](optional: java.util.Optional[A]) {
    def toOption: Option[A] = {
      if (optional.isPresent) {
        Option(optional.get)
      } else {
        None
      }
    }
  }

  implicit class RichOption[A](option: Option[A]) {
    def toOptional: java.util.Optional[A] = {
      option match {
        case None => java.util.Optional.empty()
        case Some(value) => java.util.Optional.of(value)
      }
    }
  }
}
