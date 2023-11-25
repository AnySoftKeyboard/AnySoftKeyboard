@file:JvmName("VersionGeneratorPlugin")

package net.evendanan.versiongenerator

import org.gradle.api.Plugin
import org.gradle.api.Project

class VersionGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.also {
            it.extensions.create("versionGenerator", Factory::class.java, project)
        }
    }

    //This class has to be `open` so Gradle will be able to create a Proxy to it.
    open class Factory(private val project: Project) : VersionGeneratorFactory() {
        override fun generateVersion(major: Int, minor: Int, patchOffset: Int, generators: Iterable<VersionGenerator>): VersionData {
            return finishForGradle(super.generateVersion(major, minor, patchOffset, generators))
        }

        private fun finishForGradle(version: VersionData): VersionData {
            println("Using %s for versioning.".format(version.generator.name))
            println("Generated version %s (version-code %d)".format(version.versionName, version.versionCode))

            project.version = version.versionName
            project.allprojects.forEach { it.version = version.versionName }

            return version
        }
    }
}
