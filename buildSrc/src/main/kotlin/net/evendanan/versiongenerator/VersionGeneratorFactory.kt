@file:JvmName("VersionGeneratorFactory")

package net.evendanan.versiongenerator

import net.evendanan.versiongenerator.generators.EnvBuildVersionGenerator
import net.evendanan.versiongenerator.generators.GitBuildVersionGenerator
import net.evendanan.versiongenerator.generators.StaticVersionGenerator

open class VersionGeneratorFactory {
  open fun generateVersion(
      major: Int,
      minor: Int,
      patchOffset: Int,
      generators: Iterable<VersionGenerator>
  ): VersionData {
    val generationData = GenerationData(major, minor, patchOffset)

    for (generator in generators) {
      if (generator.isValidForEnvironment()) {
        return generator.generate(generationData)
      }
    }

    throw IllegalStateException("Could not find any valid VersionGenerator for this environment!")
  }

  open fun generateVersion(
      major: Int,
      minor: Int,
      generators: Iterable<VersionGenerator>
  ): VersionData {
    return generateVersion(major, minor, 0, generators)
  }

  open fun generateVersion(major: Int, minor: Int, patchOffset: Int): VersionData {
    return generateVersion(
        major,
        minor,
        patchOffset,
        listOf(
            EnvBuildVersionGenerator.CircleCi(),
            EnvBuildVersionGenerator.Shippable(),
            GitBuildVersionGenerator(),
            StaticVersionGenerator()))
  }
}
