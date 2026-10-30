package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Simulador de Votación", appName)
  }

  @Test
  fun testDimensionsSerialization() {
    val original = DimensionsConfig(
      autoResponsive = true,
      intendenteCardHeightDp = 150,
      juntaCardHeightDp = 140,
      concejalCardHeightDp = 110,
      gridColumnsIntendente = 3,
      gridColumnsJunta = 3,
      gridColumnsConcejal = 4
    )
    val json = ConfigManager.dimensionsToJson(original)
    val parsed = ConfigManager.dimensionsFromJson(json)

    org.junit.Assert.assertNotNull(parsed)
    assertEquals(150, parsed!!.intendenteCardHeightDp)
    assertEquals(140, parsed.juntaCardHeightDp)
    assertEquals(110, parsed.concejalCardHeightDp)
    assertEquals(3, parsed.gridColumnsIntendente)
    assertEquals(3, parsed.gridColumnsJunta)
    assertEquals(4, parsed.gridColumnsConcejal)
    org.junit.Assert.assertTrue(parsed.autoResponsive)
  }

  @Test
  fun testOrientationAdaptation() {
    // Phone in Portrait (Vertical)
    val phonePortraitMetrics = evaluateScreenMetrics(390f, 844f)
    org.junit.Assert.assertTrue(phonePortraitMetrics.isPortrait)
    org.junit.Assert.assertFalse(phonePortraitMetrics.isTabletOrDesktop)
    assertEquals("Teléfono Vertical", phonePortraitMetrics.deviceCategory)

    val phonePortraitDims = computeAutoResponsiveDimensions(390f, 844f)
    assertEquals(2, phonePortraitDims.gridColumnsIntendente)
    assertEquals(2, phonePortraitDims.gridColumnsJunta)
    assertEquals(3, phonePortraitDims.gridColumnsConcejal)

    // Phone in Landscape (Horizontal)
    val phoneLandscapeMetrics = evaluateScreenMetrics(844f, 390f)
    org.junit.Assert.assertFalse(phoneLandscapeMetrics.isPortrait)
    assertEquals("Teléfono Horizontal", phoneLandscapeMetrics.deviceCategory)

    val phoneLandscapeDims = computeAutoResponsiveDimensions(844f, 390f)
    assertEquals(2, phoneLandscapeDims.gridColumnsIntendente)
    assertEquals(3, phoneLandscapeDims.gridColumnsJunta)
    assertEquals(4, phoneLandscapeDims.gridColumnsConcejal)
  }
}
