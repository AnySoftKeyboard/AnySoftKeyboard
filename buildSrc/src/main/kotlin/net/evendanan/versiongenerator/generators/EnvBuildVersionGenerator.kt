@file:JvmName("EnvBuildVersionGenerator")

package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import net.evendanan.versiongenerator.VersionGenerator

abstract class EnvBuildVersionGenerator protected constructor(name: String, private val envKey: String, private val buildNumberOffset: Int, private val patchNumberOffset: Int)
    : VersionGenerator(name) {

    override fun isValidForEnvironment(): Boolean {
        return System.getenv(envKey)?.isNotBlank() ?: false
    }

    override fun getVersionCode(generationData: GenerationData): Int {
        val buildNumberString = System.getenv(envKey)
        return Integer.parseInt(buildNumberString) + buildNumberOffset
    }

    override fun getVersionName(generationData: GenerationData): String {
        val patchedGenerationData = GenerationData(generationData.major, generationData.minor, generationData.patchOffset + patchNumberOffset)
        return super.getVersionName(patchedGenerationData)
    }

    class CircleCi(buildNumberOffset: Int, patchNumberOffset: Int) : EnvBuildVersionGenerator("CircleCiVersionGenerator", "CIRCLE_BUILD_NUM", buildNumberOffset, patchNumberOffset) {
        constructor() : this(0, 0)
    }

    class Shippable(buildNumberOffset: Int, patchNumberOffset: Int) : EnvBuildVersionGenerator("ShippableVersionGenerator", "BUILD_NUMBER", buildNumberOffset, patchNumberOffset) {
        constructor() : this(0, 0)
    }

    class Generic(buildCounterEnvKey: String, buildCounterOffset: Int, patchOffset: Int) :
            EnvBuildVersionGenerator("GenericEnvVersionGenerator",
                    buildCounterEnvKey, buildCounterOffset, patchOffset) {
        constructor(buildCounterEnvKey: String, buildCounterOffsetEnvKey: String, patchOffsetEnvKey: String) :
                this(buildCounterEnvKey,
                        System.getenv(buildCounterOffsetEnvKey)?.toIntOrNull() ?: 0,
                        System.getenv(patchOffsetEnvKey)?.toIntOrNull() ?: 0)
    }
}