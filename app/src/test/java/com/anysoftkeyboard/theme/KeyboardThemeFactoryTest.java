package com.anysoftkeyboard.theme;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardThemeFactoryTest {

    @Test
    public void testObserveCurrentThemeFiredWhenPrefChanges() {
        final KeyboardThemeFactory keyboardThemeFactory = AnyApplication.getKeyboardThemeFactory(getApplicationContext());
        AtomicReference<KeyboardTheme> currentTheme = new AtomicReference<>();
        final Disposable disposable = KeyboardThemeFactory.observeCurrentTheme(getApplicationContext()).subscribe(currentTheme::set);

        Assert.assertEquals("2fbea491-15f6-4b40-9259-06e21d9dba95", currentTheme.get().getId());

        keyboardThemeFactory.setAddOnEnabled("55d9797c-850c-40a8-9a5d-7467b55bd537", true);

        Assert.assertEquals("55d9797c-850c-40a8-9a5d-7467b55bd537", currentTheme.get().getId());

        disposable.dispose();

        keyboardThemeFactory.setAddOnEnabled("2fbea491-15f6-4b40-9259-06e21d9dba95", true);
        //still same as before
        Assert.assertEquals("55d9797c-850c-40a8-9a5d-7467b55bd537", currentTheme.get().getId());
    }
}