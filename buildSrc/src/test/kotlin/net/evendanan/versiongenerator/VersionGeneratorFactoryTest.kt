package net.evendanan.versiongenerator

import com.nhaarman.mockito_kotlin.*
import org.junit.Assert
import org.junit.Test

class VersionGeneratorFactoryTest {
  open class FakeVersionGenerator(
      name: String,
      private val enabled: Boolean,
      private val fakeVersionCode: Int
  ) : VersionGenerator(name) {
    override fun isValidForEnvironment(): Boolean {
      return enabled
    }

    override fun getVersionCode(generationData: GenerationData): Int {
      return fakeVersionCode
    }
  }

  private val generators =
      listOf(
          spy(FakeVersionGenerator("first", false, 33)),
          spy(FakeVersionGenerator("second", true, 11)),
          spy(FakeVersionGenerator("third", true, 100)))

  @Test
  fun testHappyPath() {
    val factory = VersionGeneratorFactory()

    val generateVersion = factory.generateVersion(1, 2, 3, generators)

    Assert.assertEquals(11, generateVersion.versionCode)

    verify(generators[0]).isValidForEnvironment()
    verify(generators[1]).isValidForEnvironment()
    verify(generators[2], never()).isValidForEnvironment()

    verify(generators[0], never()).generate(any())
    verify(generators[2], never()).generate(any())

    val captor = argumentCaptor<GenerationData>()

    verify(generators[1]).generate(captor.capture())

    Assert.assertEquals(1, captor.firstValue.major)
    Assert.assertEquals(2, captor.firstValue.minor)
    Assert.assertEquals(3, captor.firstValue.patchOffset)
  }

  @Test(expected = IllegalStateException::class)
  fun tesNoGenerator() {
    val factory = VersionGeneratorFactory()

    factory.generateVersion(1, 2, 3, listOf(FakeVersionGenerator("first", false, 33)))
  }
}
