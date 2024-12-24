package net.evendanan.versiongenerator.generators

import net.evendanan.versiongenerator.GenerationData
import org.junit.Assert
import org.junit.Test

class StaticGeneratorTest {

  val generationData = GenerationData(2, 0, 11)

  @Test
  fun testDefaultStaticGenerator() {
    val staticVersionGenerator = StaticVersionGenerator()
    Assert.assertTrue(staticVersionGenerator.isValidForEnvironment())
    val versionData = staticVersionGenerator.generate(generationData)
    Assert.assertEquals(1, versionData.versionCode)
    Assert.assertEquals("2.0." + (generationData.patchOffset + 1), versionData.versionName)
  }

  @Test
  fun testStaticGenerator() {
    val staticVersionGenerator = StaticVersionGenerator(32)
    Assert.assertTrue(staticVersionGenerator.isValidForEnvironment())
    val versionData = staticVersionGenerator.generate(generationData)
    Assert.assertEquals(32, versionData.versionCode)
    Assert.assertEquals("2.0." + (generationData.patchOffset + 32), versionData.versionName)
  }

  @Test
  fun testStaticGeneratorProvider() {
    val staticVersionGenerator = StaticVersionGenerator { 34 }
    Assert.assertTrue(staticVersionGenerator.isValidForEnvironment())
    val versionData = staticVersionGenerator.generate(generationData)
    Assert.assertEquals(34, versionData.versionCode)
    Assert.assertEquals("2.0." + (generationData.patchOffset + 34), versionData.versionName)
  }
}
