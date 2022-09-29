package net.evendanan.versiongenerator

import com.nhaarman.mockito_kotlin.*
import net.evendanan.versiongenerator.SimpleVersionGeneratorPlugin.Companion.FORCE_BUILD_COUNT_PROPERTY_NAME
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables

class SimpleVersionGeneratorPluginTest {
    @Rule
    @JvmField
    val environmentVariables = EnvironmentVariables()

    private val underTest = SimpleVersionGeneratorPlugin()
    private val project = mock<Project>()
    private val extensions = mock<ExtensionContainer>()

    @Before
    fun setup() {
        environmentVariables.set(CI_BUILD_COUNTER_KEY, CI_BUILD_COUNTER_VALUE)

        doReturn(extensions).`when`(project).extensions
    }

    @Test
    fun testFactoryExistsInExtension() {
        underTest.apply(project)

        verify(extensions).create(eq("autoVersioning"), eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))
    }

    @Test
    fun testDoesNotCrashOnNullApply() {
        underTest.apply(null)
    }

    @Test
    fun testRegisterAfterEval() {
        underTest.apply(project)

        verify(project).afterEvaluate(any<Action<Project>>())
    }

    @Test
    fun testFailsIfEnvironmentVariableWasNotSet() {
        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        doReturn(SimpleVersionGeneratorPlugin.SimpleConfiguration()).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        try {
            captor.lastValue.execute(project)
            Assert.fail("afterEvaluate did not fail when environment variable was empty")
        } catch (ex: IllegalArgumentException) {
            Assert.assertEquals("Provide the name of build-count environment variable using 'autoVersioning { buildCounterEnvKey }'", ex.message)
        }
    }

    @Test
    fun testTakesValueFromEnv() {
        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
            major = 2
            minor = 1
            patchOffset = 12
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        Assert.assertEquals("2.1.135", settings.versionData?.versionName)
    }

    @Test
    fun testTakesValueFromEnvAndOffsetBuildCounter() {
        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
            major = 2
            minor = 1
            patchOffset = 12
            buildCounterOffset = -17
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        Assert.assertEquals("2.1.118", settings.versionData?.versionName)
    }

    @Test
    fun testOnlyUsesStaticIfDisabled() {
        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            enabled = false
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
            major = 2
            minor = 1
            patchOffset = 12
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        //note: patch offset is only set to non-static
        Assert.assertEquals("2.1.1", settings.versionData?.versionName)
    }

    @Test
    fun testTakesStaticIfEnvNotAvailable() {
        environmentVariables.set(CI_BUILD_COUNTER_KEY, null)

        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        Assert.assertEquals("1.0.1", settings.versionData?.versionName)
    }

    @Test
    fun testTakeForceValueWhenSpecified() {
        doReturn(true).`when`(project).hasProperty(eq(FORCE_BUILD_COUNT_PROPERTY_NAME))
        doReturn("120").`when`(project).property(eq(FORCE_BUILD_COUNT_PROPERTY_NAME))

        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
            major = 2
            minor = 1
            patchOffset = 12
            buildCounterOffset = -17
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        //we should get 115 since we forced the build-count to 120 (env-variable value is 123)
        Assert.assertEquals("2.1.115", settings.versionData?.versionName)
    }

    @Test
    fun testUsesOverrideDefaultBuildCount() {
        environmentVariables.set(CI_BUILD_COUNTER_KEY, null)

        underTest.apply(project)

        val captor = argumentCaptor<Action<Project>>()
        verify(project).afterEvaluate(captor.capture())

        val settings = SimpleVersionGeneratorPlugin.SimpleConfiguration().apply {
            buildCounterEnvKey = CI_BUILD_COUNTER_KEY
            defaultBuildCount = 10
        }

        doReturn(settings).`when`(extensions).getByType(eq(SimpleVersionGeneratorPlugin.SimpleConfiguration::class.java))

        captor.lastValue.execute(project)

        Assert.assertNotNull(settings.versionData)
        Assert.assertEquals("1.0.10", settings.versionData?.versionName)
    }

    companion object {
        private const val CI_BUILD_COUNTER_VALUE = "123"
        private const val CI_BUILD_COUNTER_KEY = "SimpleVersionGeneratorPluginTest_BUILD_COUNTER"
    }
}