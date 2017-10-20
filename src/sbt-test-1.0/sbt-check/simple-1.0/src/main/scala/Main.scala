object Main {
  @deprecated("foo", since = "0.1.0")
  def foo(): Unit = {
    /* no-op */
  }

  def main(args: Array[String]): Unit = {
    foo()
  }
}
