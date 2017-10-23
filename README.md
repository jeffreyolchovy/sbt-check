# sbt-check
An sbt plugin that provides the `check` task.

`check` compiles Scala sources with the `scalac` option `-Ystop-after:typer`.

For more information, see <em>[Would scalac/sbt benefit from something like Rust 1.16's "cargo check" subcommand?](https://www.reddit.com/r/scala/comments/5ztjrl/would_scalacsbt_benefit_from_something_like_rust/)</em>.

Compatible with sbt 0.13.x and 1.0.x.

## Usage
Add the following to your `project/plugins.sbt` file:

```
addSbtPlugin("com.github.jeffreyolchovy" % "sbt-check" % "0.1.1")
```

For JVM projects, the `check` task will now be available.
