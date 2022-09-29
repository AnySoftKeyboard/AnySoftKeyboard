@file:JvmName("VersionGenerator")

package net.evendanan.versiongenerator

abstract class VersionGenerator(val name: String) {

    abstract fun isValidForEnvironment(): Boolean

    open fun generate(generationData: GenerationData): VersionData {
        return VersionData(getVersionCode(generationData), getVersionName(generationData), this)
    }

    protected abstract fun getVersionCode(generationData: GenerationData): Int

    protected open fun getVersionName(generationData: GenerationData): String {
        return "%d.%d.%d".format(
                generationData.major,
                generationData.minor,
                getVersionCode(generationData) + generationData.patchOffset)
    }
}
