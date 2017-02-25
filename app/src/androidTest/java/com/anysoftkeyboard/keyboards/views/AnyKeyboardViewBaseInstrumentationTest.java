package com.anysoftkeyboard.keyboards.views;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;

import com.anysoftkeyboard.gesturetyping.GestureTypingDebugUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

@RunWith(AndroidJUnit4.class)
@SmallTest
/**
 * Handle running tests that require a keyboard view, putting screenshots
 * in /sdcard/ask/_name_.png
 */
public class AnyKeyboardViewBaseInstrumentationTest {
    protected OnKeyboardActionListener mMockKeyboardListener;
    protected AnyKeyboardViewBase mUnderTest;
    protected AnyKeyboard mEnglishKeyboard;
    protected Context context;

    private int width = 1000, height = 1000;

    // Avoid deleting screenshots after every test
    private static boolean setupIsDone = false;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext().getApplicationContext();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
                AnyKeyboardViewBase view = createViewToTest(context);
                setCreatedKeyboardView(view);
                mUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);

                mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(context)
                        .get(0)
                        .createKeyboard(context, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
                mEnglishKeyboard.loadKeyboard(mUnderTest.getThemedKeyboardDimens());

                mUnderTest.setKeyboard(mEnglishKeyboard, 0);

                LinearLayout layout = new LinearLayout(context);
                layout.addView(mUnderTest);

                ViewHelpers.setupView(layout)
                        .setExactWidthDp(2000)
                        .setExactHeightDp(2000)
                        .layout();

                prepScreenshotDir();
            }
        });
    }

    @CallSuper
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        mUnderTest = view;
    }

    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardViewBase(context, null);
    }

    protected void prepScreenshotDir() {
        if (setupIsDone) return;

        // The gradle task should handle this
        Assert.assertTrue(ContextCompat.checkSelfPermission(InstrumentationRegistry.getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED);
        Assert.assertTrue(ContextCompat.checkSelfPermission(InstrumentationRegistry.getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED);
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/ask/");
        Assert.assertTrue(f.mkdir() || f.isDirectory());
        for (File child : f.listFiles()) {
            if (child.isFile()) Assert.assertTrue(child.delete());
        }

        setupIsDone = true;
    }

    protected void saveScreenshot(String name) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mUnderTest.draw(canvas);
        GestureTypingDebugUtils.drawGestureDebugInfo(canvas);
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/ask/" + name + ".png");
            FileOutputStream fos = new FileOutputStream(f);
            Assert.assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos));
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
