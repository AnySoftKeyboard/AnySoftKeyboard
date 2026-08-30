package com.anysoftkeyboard.addon.apk

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner
import com.anysoftkeyboard.addon.base.apk.R
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(AnySoftKeyboardRobolectricTestRunner::class)
class MainActivityBaseTest {
  @get:Rule val composeRule = createEmptyComposeRule()

  @Test
  fun testActivityShowsAddOnDetails() {
    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED)
      composeRule.onNodeWithText("Thank you for installing Test Add On App Name.").assertExists()
      composeRule.onNodeWithContentDescription("A screenshot of the addon").assertExists()
      composeRule
          .onNodeWithText("This is a test add on description, it can be anything")
          .assertExists()
      composeRule.onNodeWithText("Visit us at https://example.com").assertExists()
      composeRule
          .onNodeWithText(
              """Release notes for vnull (0):
* this
* and that""",
          )
          .assertExists()
    }
  }

  @Test
  fun testInstallAnySoftKeyboardFlow() {
    Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
        .deletePackage(ASK_PACKAGE_NAME)

    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED)
      composeRule
          .onNodeWithText(
              "AnySoftKeyboard is not installed on your device.\n" +
                  "In order to use this expansion pack, " +
                  "you must first install AnySoftKeyboard.",
          )
          .assertExists()

      composeRule.onNodeWithText("Go to Play Store").performScrollTo().performClick()

      val app = Shadows.shadowOf(RuntimeEnvironment.getApplication())
      val searchIntent = app.nextStartedActivity
      Assert.assertNotNull(searchIntent)
      Assert.assertEquals(Intent.ACTION_VIEW, searchIntent.action)
      Assert.assertEquals("market", searchIntent.data?.scheme)
      Assert.assertEquals("search", searchIntent.data?.authority)
      Assert.assertEquals("q=com.menny.android.anysoftkeyboard", searchIntent.data?.query)
    }
  }

  @Test
  fun testAlreadyInstalledAnySoftKeyboardFlow() {
    Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager).let { pm ->
      PackageInfo().let { info ->
        info.packageName = ASK_PACKAGE_NAME
        pm.installPackage(info)
      }
      ComponentName(ASK_PACKAGE_NAME, "${ASK_PACKAGE_NAME}.MainActivity").let { info ->
        pm.addActivityIfNotPresent(info)
        pm.addIntentFilterForActivity(
            info,
            android.content.IntentFilter().apply {
              addAction(Intent.ACTION_MAIN)
              addCategory(Intent.CATEGORY_LAUNCHER)
            },
        )
      }
    }

    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED)
      composeRule
          .onNodeWithText(
              "AnySoftKeyboard is installed. You may need to set it up to start using this expansion pack.",
          )
          .assertExists()

      composeRule.onNodeWithText("Open AnySoftKeyboard").performScrollTo().performClick()

      val app = Shadows.shadowOf(RuntimeEnvironment.getApplication())
      val launcherIntent = app.nextStartedActivity
      Assert.assertNotNull(launcherIntent)
      Assert.assertEquals(ASK_PACKAGE_NAME, launcherIntent.`package`)
    }
  }

  @Test
  fun testIsAnySoftKeyboardInstalledReturnsFalseWhenNotInstalled() {
    Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
        .deletePackage(ASK_PACKAGE_NAME)

    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED).onActivity { activity ->
        Assert.assertFalse(activity.isAnySoftKeyboardInstalled())
      }
      composeRule
          .onNodeWithText(
              "AnySoftKeyboard is not installed on your device.\n" +
                  "In order to use this expansion pack, " +
                  "you must first install AnySoftKeyboard.",
          )
          .assertExists()
    }
  }

  @Test
  fun testIsAnySoftKeyboardInstalledReturnsTrueWhenInstalledButNotEnabled() {
    Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager).let { pm ->
      PackageInfo().let { info ->
        info.packageName = ASK_PACKAGE_NAME
        pm.installPackage(info)
      }
    }

    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED).onActivity { activity ->
        Assert.assertTrue(activity.isAnySoftKeyboardInstalled())
      }
      composeRule
          .onNodeWithText(
              "AnySoftKeyboard is installed. You may need to set it up to start using this expansion pack.",
          )
          .assertExists()
    }
  }

  @Test
  fun testHideLauncherIconFlow() {
    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED).onActivity { activity ->
        val launcherComponent =
            ComponentName(activity.packageName, "${activity.packageName}.LauncherAlias")
        Assert.assertNotEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            activity.packageManager.getComponentEnabledSetting(launcherComponent),
        )

        composeRule.onNodeWithText("Hide icon from launcher").performScrollTo().performClick()

        Assert.assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            activity.packageManager.getComponentEnabledSetting(launcherComponent),
        )
        Assert.assertTrue(activity.isFinishing)
      }
    }
  }

  @Test
  fun testClickWebsiteOpensBrowser() {
    ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED)
      composeRule.onNodeWithText("Visit us at https://example.com").performScrollTo().performClick()

      val app = Shadows.shadowOf(RuntimeEnvironment.getApplication())
      val browserIntent = app.nextStartedActivity
      Assert.assertNotNull(browserIntent)
      Assert.assertEquals(Intent.ACTION_VIEW, browserIntent.action)
      Assert.assertEquals("https://example.com", browserIntent.dataString)
    }
  }
}

class TestMainActivity :
    MainActivityBase(
        R.string.test_app_name,
        R.string.test_add_on_description,
        R.string.test_web_site,
        R.string.test_release_notes,
        R.drawable.test_screenshot,
    )
