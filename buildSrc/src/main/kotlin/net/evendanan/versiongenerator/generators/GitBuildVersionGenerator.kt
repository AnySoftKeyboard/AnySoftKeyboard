@file:JvmName("GitBuildVersionGenerator")

package net.evendanan.versiongenerator.generators

import java.io.IOException
import java.util.concurrent.TimeUnit
import net.evendanan.versiongenerator.GenerationData
import net.evendanan.versiongenerator.VersionGenerator

class GitBuildVersionGenerator(
    private val processRunner: ProcessOutput,
    private val buildNumberOffset: Int,
    private val patchNumberOffset: Int
) : VersionGenerator("GitVersionBuilder") {

  constructor(
      buildNumberOffset: Int,
      patchNumberOffset: Int
  ) : this(DefaultProcessRunner, buildNumberOffset, patchNumberOffset)

  constructor() : this(0, 0)

  override fun isValidForEnvironment(): Boolean {
    return getGitHistorySize() > 0
  }

  override fun getVersionCode(generationData: GenerationData): Int {
    val revCount = getGitHistorySize()
    val tagCount = processRunner.runCommandForOutput("git tag --merged").split("\n").size

    return revCount + tagCount + buildNumberOffset
  }

  override fun getVersionName(generationData: GenerationData): String {
    val patchedGenerationData =
        GenerationData(
            generationData.major,
            generationData.minor,
            generationData.patchOffset + patchNumberOffset)
    return super.getVersionName(patchedGenerationData)
  }

  private fun getGitHistorySize(): Int {
    return try {
      Integer.parseInt(processRunner.runCommandForOutput("git rev-list --count HEAD"))
    } catch (e: Exception) {
      -1
    }
  }

  private object DefaultProcessRunner : ProcessOutput {
    override fun runCommandForOutput(command: String): String {
      return try {
        val parts = command.split("\\s".toRegex())
        val proc =
            ProcessBuilder(*parts.toTypedArray())
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        proc.inputStream.bufferedReader().readText().trim()
      } catch (e: IOException) {
        println("runCommand IOException %s".format(e))
        e.printStackTrace()
        ""
      }
    }
  }

  interface ProcessOutput {
    fun runCommandForOutput(command: String): String
  }
}
