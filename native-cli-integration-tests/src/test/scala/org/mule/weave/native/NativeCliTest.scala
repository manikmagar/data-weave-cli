package org.mule.weave.native

import org.mule.weave.v2.utils.DataWeaveVersion
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FreeSpec
import org.scalatest.Matchers

import java.io.File
import java.net.URL

class NativeCliTest extends FreeSpec
  with Matchers
  with BeforeAndAfterAll
  with ResourceResolver {

  private lazy val USER_HOME: File = new File(System.getProperty("user.home"))

  private lazy val DEFAULT_DW_CLI_HOME: File = {
    val home = new File(USER_HOME, ".dw")
    home
  }

  override protected def beforeAll(): Unit = {
    DEFAULT_DW_CLI_HOME.mkdirs()
  }

  "it should execute simple migration correctly" in {
    val stream: URL = getClass.getClassLoader.getResource("dw1/SimpleFile.dw1")
    val file = new File(stream.toURI)
    val (_, output, _) = NativeCliITTestRunner(Array("migrate", file.getAbsolutePath)).execute()
    output.trim shouldBe
      """
        |%dw 2.0
        |var arr = ["foo"]
        |---
        |{
        |  a: [0, 1, 2] contains [1, 2],
        |  b: sum(1 to 1000),
        |  c: sizeOf(("123"))
        |}
        |""".stripMargin.trim
  }

  "it should execute simple case correctly" in {
    val (_, output, _) = NativeCliITTestRunner(Array("run", "1 to 10")).execute()
    output shouldBe "[\n  1,\n  2,\n  3,\n  4,\n  5,\n  6,\n  7,\n  8,\n  9,\n  10\n]"
  }

  "it should execute with input" in {
    val path = getResourcePath("inputs/payload.json")
    val (_, output, _) = NativeCliITTestRunner(Array("run", "-i", "payload=" + path, "payload.name")).execute()
    output shouldBe "\"Tomo\""
  }

  "it should execute with input and script" in {
    val inputPath = getResourcePath("inputs/payload.json")
    val transformationPath = getResourcePath("scripts/GetName.dwl")
    val (_, output, _) = NativeCliITTestRunner(Array("run", "-i", "payload=" + inputPath, "-f", transformationPath)).execute()
    output shouldBe "\"Tomo\""
  }

  "it should fail if language level is set incorrectly" in {
    val (exitCode, _, errorMsg) = NativeCliITTestRunner(Array("run", "--language-level=payload", "1")).execute()
    exitCode should not be 0
    errorMsg should include("Invalid language-level option value : `payload`")
  }

  "should fail if language level is greater than runtime" in {
    val runtimeLL = DataWeaveVersion()
    val badLL = s"${runtimeLL.major}.${runtimeLL.minor + 1}"
    val (exitCode, test, errorMsg) = NativeCliITTestRunner(Array("run", "--language-level=" + badLL, "1")).execute()
    errorMsg should include(s"Invalid language level, cannot be higher than ${runtimeLL.toString()}")
  }
}
