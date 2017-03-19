# sbt-check
An sbt plugin that provides the `check` task.

`check` compiles Scala sources with the `scalac` option `-Ystop-after:typer".

For more information, see https://www.reddit.com/r/scala/comments/5ztjrl/would_scalacsbt_benefit_from_something_like_rust/.

## Usage
Add the following to your `project/plugins.sbt` file:

```
resolvers += Resolver.bintrayRepo("jeffreyolchovy", "sbt-plugins")

addSbtPlugin("com.github.jeffreyolchovy" % "sbt-check" % "0.1.0")
```

For JVM projects, the `check` task will now be available.
