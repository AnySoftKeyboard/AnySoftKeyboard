package com.anysoftkeyboard.addon.apk

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner
import com.anysoftkeyboard.addon.base.apk.R
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(AnySoftKeyboardRobolectricTestRunner::class)
class MainActivityBaseTest {

    @Test
    fun testActivityShowsAddOnDetails() {
        ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
            scenario
                .moveToState(Lifecycle.State.RESUMED)
                .onActivity { activity ->
                    activity.findViewById<TextView>(R.id.welcome_description).let {
                        Assert.assertEquals(
                            "Thank you for installing Test Add On App Name for AnySoftKeyboard.",
                            it.text,
                        )
                    }
                    activity.findViewById<ImageView>(R.id.app_screenshot).let {
                        Assert.assertEquals(
                            R.drawable.test_screenshot,
                            Shadows.shadowOf(it.drawable).createdFromResId,
                        )
                    }
                    activity.findViewById<TextView>(R.id.pack_description).let {
                        Assert.assertEquals(
                            "This is a test add on description, it can be anything",
                            it.text,
                        )
                    }
                }
        }
    }

    @Test
    fun testInstallAnySoftKeyboardFlow() {
        Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager)
            .deletePackage(ASK_PACKAGE_NAME)

        ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
            scenario
                .moveToState(Lifecycle.State.RESUMED)
                .onActivity { activity ->
                    activity.findViewById<TextView>(R.id.action_description).run {
                        Assert.assertEquals(
                            "AnySoftKeyboard is not installed on your device.\nIn order to use this expansion pack, you must first install AnySoftKeyboard.",
                            text,
                        )
                    }
                    activity.findViewById<Button>(R.id.action_button).run {
                        Assert.assertEquals(
                            "Go to Play Store",
                            text,
                        )
                        Shadows.shadowOf(this).onClickListener.onClick(this)
                        Shadows.shadowOf(RuntimeEnvironment.getApplication()).let { app ->
                            app.nextStartedActivity.let { searchIntent ->
                                Assert.assertEquals(Intent.ACTION_VIEW, searchIntent.action)
                                Assert.assertEquals("market", searchIntent.data!!.scheme)
                                Assert.assertEquals("search", searchIntent.data!!.authority)
                                Assert.assertEquals(
                                    "q=com.menny.android.anysoftkeyboard",
                                    searchIntent.data!!.query,
                                )
                            }
                        }
                    }
                }
        }
    }

    @Test
    fun testAlreadyInstalledAnySoftKeyboardFlow() {
        Shadows.shadowOf(RuntimeEnvironment.getApplication().packageManager).let { pm ->
            PackageInfo().let { info ->
                info.packageName = ASK_PACKAGE_NAME
                pm.installPackage(info)
            }
            pm.addServiceIfNotPresent(
                ComponentName(
                    ASK_PACKAGE_NAME,
                    "${ASK_PACKAGE_NAME}.SoftKeyboard",
                ),
            )
            ComponentName(ASK_PACKAGE_NAME, "${ASK_PACKAGE_NAME}.MainActivity").let { info ->
                pm.addActivityIfNotPresent(info)
                pm.addIntentFilterForActivity(
                    info,
                    IntentFilter().apply {
                        addAction(Intent.ACTION_MAIN)
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    },
                )
            }
        }

        ActivityScenario.launch(TestMainActivity::class.java).use { scenario ->
            scenario
                .moveToState(Lifecycle.State.RESUMED)
                .onActivity { activity ->
                    activity.findViewById<TextView>(R.id.action_description).run {
                        Assert.assertEquals(
                            "AnySoftKeyboard is installed. You may need to set it up to start using this expansion pack.",
                            text,
                        )
                    }
                    activity.findViewById<Button>(R.id.action_button).run {
                        Assert.assertEquals(
                            "Open AnySoftKeyboard",
                            text,
                        )
                        Shadows.shadowOf(this).onClickListener.onClick(this)
                        Shadows.shadowOf(RuntimeEnvironment.getApplication()).let { app ->
                            app.nextStartedActivity.let { launcherIntent ->
                                Assert.assertEquals(ASK_PACKAGE_NAME, launcherIntent.`package`)
                            }
                        }
                    }
                }
        }
    }
}

class TestMainActivity : MainActivityBase(
    R.string.test_app_name,
    R.string.test_add_on_description,
    R.drawable.test_screenshot,
)
