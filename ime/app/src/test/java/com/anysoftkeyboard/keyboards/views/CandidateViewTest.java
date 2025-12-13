package com.anysoftkeyboard.keyboards.views;

import android.graphics.Color;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class CandidateViewTest {

  @Test
  public void test_whenOverlayHasTransparentBlack_thenNormalizationIsNotApplied() {
    OverlayData overlay = new OverlayDataImpl();
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "The overlay should not have been normalized, so the original "
            + "PrimaryTextColor should have been retained",
        Color.TRANSPARENT,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "The overlay should not have been normalized, so the original "
            + "SecondaryTextColor should have been retained",
        Color.TRANSPARENT,
        normalizedOverlay.getSecondaryTextColor());
  }

  @Test
  public void test_whenOverlayHasAllDarkColors_thenNormalizationToLightTextIsApplied() {
    OverlayData overlay =
        new OverlayDataImpl(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "The overlay should have been normalized to dark background with " + "light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "The overlay should have been normalized to dark background with " + "light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  @Test
  public void test_whenOverlayHasAllLightColors_thenNormalizationToDarkTextIsApplied() {
    OverlayData overlay =
        new OverlayDataImpl(Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "The overlay should have been normalized to light background with " + "dark text",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "The overlay should have been normalized to light background with " + "dark text",
        Color.DKGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Small contrast difference that requires normalization
  @Test
  public void test_whenOverlayHasSmallContrastDifference_thenNormalizationIsApplied() {
    // Dark background with slightly lighter dark text (diff < 96)
    int darkBackground = Color.rgb(20, 20, 20);
    int slightlyLighterText = Color.rgb(40, 40, 40);
    OverlayData overlay =
        new OverlayDataImpl(
            Color.BLACK, darkBackground, Color.BLUE, slightlyLighterText, slightlyLighterText);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Small contrast should trigger normalization to light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Small contrast should trigger normalization to light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Sufficient contrast should NOT trigger normalization
  @Test
  public void test_whenOverlayHasSufficientContrast_thenNormalizationIsNotApplied() {
    // getNormalizedOverlayData checks contrast between primaryDarkColor and secondaryTextColor
    // High contrast: white primaryDarkColor with black secondaryTextColor (diff = 255)
    int primaryDarkColor = Color.WHITE; // luminance ≈ 255
    int secondaryTextColor = Color.BLACK; // luminance = 0
    int primaryTextColor = Color.rgb(30, 30, 30); // Dark gray for primary text
    // Need valid overlay: primaryColor != primaryTextColor AND primaryDarkColor != primaryTextColor
    OverlayData overlay =
        new OverlayDataImpl(
            Color.BLUE, primaryDarkColor, Color.GREEN, primaryTextColor, secondaryTextColor);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // High contrast (255 > 96) and valid overlay should NOT be normalized
    Assert.assertEquals(
        "High contrast valid overlay should not be normalized",
        primaryTextColor,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "High contrast valid overlay should not be normalized",
        secondaryTextColor,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Invalid overlay (primaryColor == primaryTextColor)
  @Test
  public void test_whenOverlayIsInvalidWithMatchingPrimaryColors_thenNormalizationIsApplied() {
    int sameColor = Color.rgb(100, 100, 100);
    // primaryColor == primaryTextColor makes overlay invalid
    OverlayData overlay =
        new OverlayDataImpl(sameColor, Color.BLACK, Color.BLUE, sameColor, Color.GRAY);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Invalid overlay should be normalized based on primaryDarkColor luminance
    Assert.assertEquals(
        "Invalid overlay should be normalized",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Invalid overlay should be normalized",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Invalid overlay (primaryDarkColor == primaryTextColor)
  @Test
  public void test_whenOverlayIsInvalidWithMatchingDarkColors_thenNormalizationIsApplied() {
    int sameColor = Color.rgb(50, 50, 50);
    // primaryDarkColor == primaryTextColor makes overlay invalid
    OverlayData overlay =
        new OverlayDataImpl(Color.BLUE, sameColor, Color.GREEN, sameColor, Color.LTGRAY);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Invalid overlay should be normalized based on primaryDarkColor luminance
    Assert.assertEquals(
        "Invalid overlay should be normalized",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Invalid overlay should be normalized",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Gray boundary - exactly at gray luminance
  @Test
  public void test_whenOverlayHasGrayBackground_thenNormalizationToLightTextIsApplied() {
    // GRAY has luminance around 128 which is the boundary
    // Slightly below gray luminance should use light text
    int slightlyDarkGray = Color.rgb(127, 127, 127);
    OverlayData overlay =
        new OverlayDataImpl(
            Color.BLACK, slightlyDarkGray, Color.BLUE, slightlyDarkGray, slightlyDarkGray);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Below gray luminance should use light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Below gray luminance should use light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Slightly above gray luminance
  @Test
  public void test_whenOverlayHasLightGrayBackground_thenNormalizationToDarkTextIsApplied() {
    // Slightly above gray luminance should use dark text
    int slightlyLightGray = Color.rgb(200, 200, 200);
    OverlayData overlay =
        new OverlayDataImpl(
            Color.WHITE, slightlyLightGray, Color.BLUE, slightlyLightGray, slightlyLightGray);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Above gray luminance should use dark text",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Above gray luminance should use dark text",
        Color.DKGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Real-world color - dark blue background
  @Test
  public void test_whenOverlayHasDarkBlueBackground_thenNormalizationToLightTextIsApplied() {
    int darkBlue = Color.rgb(0, 0, 139); // Dark blue
    OverlayData overlay = new OverlayDataImpl(Color.BLUE, darkBlue, Color.CYAN, darkBlue, darkBlue);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Dark blue background should use light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Dark blue background should use light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Real-world color - light yellow background
  @Test
  public void test_whenOverlayHasLightYellowBackground_thenNormalizationToDarkTextIsApplied() {
    int lightYellow = Color.rgb(255, 255, 200); // Light yellow
    OverlayData overlay =
        new OverlayDataImpl(Color.YELLOW, lightYellow, Color.BLUE, lightYellow, lightYellow);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Light yellow background should use dark text",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Light yellow background should use dark text",
        Color.DKGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Verify other overlay colors are preserved (not modified)
  @Test
  public void test_whenOverlayIsNormalized_thenOtherColorsArePreserved() {
    int primaryColor = Color.RED;
    int primaryDarkColor = Color.rgb(139, 0, 0); // Dark red
    int accentColor = Color.GREEN;
    OverlayData overlay =
        new OverlayDataImpl(primaryColor, primaryDarkColor, accentColor, Color.BLACK, Color.BLACK);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Text colors should be normalized
    Assert.assertEquals(
        "Should normalize to light text for dark background",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Should normalize to light text for dark background",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());

    // Other colors should be preserved
    Assert.assertEquals(
        "Primary color should be preserved", primaryColor, normalizedOverlay.getPrimaryColor());
    Assert.assertEquals(
        "Primary dark color should be preserved",
        primaryDarkColor,
        normalizedOverlay.getPrimaryDarkColor());
    Assert.assertEquals(
        "Accent color should be preserved", accentColor, normalizedOverlay.getAccentColor());
  }

  // Edge case: Colors with alpha channel
  @Test
  public void test_whenOverlayHasColorsWithAlpha_thenNormalizationUsesRgbComponent() {
    // Semi-transparent black (alpha = 128)
    int semiTransparentBlack = Color.argb(128, 0, 0, 0);
    OverlayData overlay =
        new OverlayDataImpl(
            semiTransparentBlack,
            semiTransparentBlack,
            Color.BLUE,
            semiTransparentBlack,
            semiTransparentBlack);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Normalization should be based on RGB values (black), not alpha
    Assert.assertEquals(
        "Semi-transparent black should still use light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Semi-transparent black should still use light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Very low luminance (almost black)
  @Test
  public void test_whenOverlayHasVeryDarkBackground_thenNormalizationToLightTextIsApplied() {
    int almostBlack = Color.rgb(1, 1, 1);
    OverlayData overlay =
        new OverlayDataImpl(Color.BLACK, almostBlack, Color.BLUE, almostBlack, almostBlack);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Almost black background should use light text",
        Color.WHITE,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Almost black background should use light text",
        Color.LTGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Very high luminance (almost white)
  @Test
  public void test_whenOverlayHasVeryLightBackground_thenNormalizationToDarkTextIsApplied() {
    int almostWhite = Color.rgb(254, 254, 254);
    OverlayData overlay =
        new OverlayDataImpl(Color.WHITE, almostWhite, Color.BLUE, almostWhite, almostWhite);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    Assert.assertEquals(
        "Almost white background should use dark text",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Almost white background should use dark text",
        Color.DKGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Mixed colors - light background, dark text with good contrast
  @Test
  public void test_whenOverlayHasGoodContrastWithLightBackground_thenNoNormalization() {
    // getNormalizedOverlayData checks contrast between primaryDarkColor and secondaryTextColor
    int primaryDarkColor = Color.rgb(230, 230, 230); // Light gray, luminance ≈ 230
    int secondaryTextColor = Color.rgb(50, 50, 50); // Dark gray, luminance ≈ 50
    int primaryTextColor = Color.rgb(40, 40, 40); // Darker gray for primary text
    // Need valid overlay: primaryColor != primaryTextColor AND primaryDarkColor != primaryTextColor
    OverlayData overlay =
        new OverlayDataImpl(
            Color.WHITE, primaryDarkColor, Color.BLUE, primaryTextColor, secondaryTextColor);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Good contrast (230 - 50 = 180 luminance diff > 96) should not normalize
    Assert.assertEquals(
        "Good contrast should not normalize",
        primaryTextColor,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Good contrast should not normalize",
        secondaryTextColor,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Exact boundary of normalization threshold (96)
  @Test
  public void test_whenOverlayHasExactThresholdContrast_thenNoNormalization() {
    // getNormalizedOverlayData uses primaryDarkColor and secondaryTextColor for contrast check
    // Create colors with exactly 96 luminance difference
    // Gray luminance is approximately: R*0.2126 + G*0.7152 + B*0.0722
    // For uniform grays: luminance ≈ grayValue * 1.0 = grayValue
    int primaryDarkColor = Color.rgb(150, 150, 150); // luminance ≈ 150
    int secondaryTextColor = Color.rgb(54, 54, 54); // luminance ≈ 54, diff ≈ 96
    // Need valid overlay: primaryColor != primaryTextColor AND primaryDarkColor != primaryTextColor
    OverlayData overlay =
        new OverlayDataImpl(
            Color.WHITE, primaryDarkColor, Color.BLUE, Color.BLACK, secondaryTextColor);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // At exact threshold (diff >= 96), should NOT normalize
    // Original colors should be preserved
    Assert.assertEquals(
        "At exact threshold should not normalize",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "At exact threshold should not normalize",
        secondaryTextColor,
        normalizedOverlay.getSecondaryTextColor());
  }

  // Edge case: Just below normalization threshold
  @Test
  public void test_whenOverlayHasJustBelowThresholdContrast_thenNormalizationIsApplied() {
    // getNormalizedOverlayData uses primaryDarkColor and secondaryTextColor for contrast check
    // Create colors with ~95 luminance difference (just below 96 threshold)
    int primaryDarkColor = Color.rgb(150, 150, 150); // luminance ≈ 150
    int secondaryTextColor = Color.rgb(55, 55, 55); // luminance ≈ 55, diff ≈ 95
    // Need valid overlay: primaryColor != primaryTextColor AND primaryDarkColor != primaryTextColor
    OverlayData overlay =
        new OverlayDataImpl(
            Color.WHITE, primaryDarkColor, Color.BLUE, Color.BLACK, secondaryTextColor);
    OverlayData normalizedOverlay = CandidateView.getNormalizedOverlayData(overlay);

    // Just below threshold should normalize (primaryDarkColor luminance 150 > GRAY_LUM, so dark
    // text)
    Assert.assertEquals(
        "Just below threshold should normalize",
        Color.BLACK,
        normalizedOverlay.getPrimaryTextColor());
    Assert.assertEquals(
        "Just below threshold should normalize",
        Color.DKGRAY,
        normalizedOverlay.getSecondaryTextColor());
  }
}
