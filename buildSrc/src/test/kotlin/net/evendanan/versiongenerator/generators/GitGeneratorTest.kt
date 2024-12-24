package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import org.junit.Assert
import org.junit.Test

class GitGeneratorTest {
  val generationData = GenerationData(0, 11, 1)

  object fakeProcessRunner : GitBuildVersionGenerator.ProcessOutput {
    override fun runCommandForOutput(command: String): String {
      if (command.startsWith("git tag")) {
        val builder = StringBuilder("0tag")
        for (i in 1..12) builder.append('\n').append(i).append("tag")

        return builder.toString()
      } else if (command.startsWith("git rev-list")) {
        return "93"
      } else {
        throw IllegalArgumentException("No idea what to do with '%s'".format(command))
      }
    }
  }

  object fakeNoGitProcessRunner : GitBuildVersionGenerator.ProcessOutput {
    override fun runCommandForOutput(command: String): String {
      return ""
    }
  }

  @Test
  fun testNoOffsetGitGenerator() {
    val gitVersionGenerator = GitBuildVersionGenerator(fakeProcessRunner, 0, 1)
    Assert.assertTrue(gitVersionGenerator.isValidForEnvironment())
    val versionData = gitVersionGenerator.generate(generationData)
    Assert.assertEquals(13 + 93, versionData.versionCode)
    Assert.assertEquals(
        "0.11." + (generationData.patchOffset + 13 + 93 + 1), versionData.versionName)
  }

  @Test
  fun testWithOffsetGitGenerator() {
    val gitVersionGenerator = GitBuildVersionGenerator(fakeProcessRunner, 10, 0)
    Assert.assertTrue(gitVersionGenerator.isValidForEnvironment())
    val versionData = gitVersionGenerator.generate(generationData)
    Assert.assertEquals(13 + 93 + 10, versionData.versionCode)
    Assert.assertEquals(
        "0.11." + (generationData.patchOffset + 13 + 93 + 10), versionData.versionName)
  }

  @Test
  fun testNoGitGenerator() {
    val gitVersionGenerator = GitBuildVersionGenerator(fakeNoGitProcessRunner, 10, -4)
    Assert.assertFalse(gitVersionGenerator.isValidForEnvironment())
  }
}
