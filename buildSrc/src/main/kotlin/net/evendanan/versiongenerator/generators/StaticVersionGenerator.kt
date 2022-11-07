@file:JvmName("StaticVersionGenerator")

package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import net.evendanan.versiongenerator.VersionGenerator

class StaticVersionGenerator(private val staticVersionCodeProvider: () -> Int)
    : VersionGenerator("StaticVersionGenerator") {
    constructor(staticValue: Int) : this({ staticValue })
    constructor() : this(1)

    override fun getVersionCode(generationData: GenerationData): Int {
        return staticVersionCodeProvider()
    }

    override fun isValidForEnvironment(): Boolean {
        return true
    }
}