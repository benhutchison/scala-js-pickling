myScalaJSSettings

name := "Scala.js pickling tests"

libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test",
    "com.lihaoyi.utest" % "utest_2.10" % "0.1.1" % "test",
    "com.lihaoyi.utest" % "utest-runner_2.10" % "0.1.1" % "test"
)
