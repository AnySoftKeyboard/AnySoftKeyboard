package com.menny.android.anysoftkeyboard;

import android.support.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import org.mockito.Mockito;

public class AnyRoboApplication extends AnyApplication {
    private ExternalDictionaryFactory mDictionaryFactory;
    private QuickTextKeyFactory mQuickKeyFactory;
    private KeyboardExtensionFactory mToolsKeyboardFactory;
    private KeyboardExtensionFactory mBottomRowFactory;
    private KeyboardExtensionFactory mTopRowFactory;
    private KeyboardFactory mKeyboardFactory;
    private KeyboardThemeFactory mThemeFactory;

    @Override
    public void onCreate() {
        TextEntryState.restartSession();
        super.onCreate();
    }

    @NonNull
    @Override
    protected ExternalDictionaryFactory createExternalDictionaryFactory() {
        return mDictionaryFactory = Mockito.spy(super.createExternalDictionaryFactory());
    }

    @NonNull
    @Override
    protected KeyboardExtensionFactory createBottomKeyboardExtensionFactory() {
        return mBottomRowFactory = Mockito.spy(super.createBottomKeyboardExtensionFactory());
    }

    @NonNull
    @Override
    protected KeyboardExtensionFactory createToolsKeyboardExtensionFactory() {
        return mToolsKeyboardFactory = Mockito.spy(super.createToolsKeyboardExtensionFactory());
    }

    @NonNull
    @Override
    protected KeyboardExtensionFactory createTopKeyboardExtensionFactory() {
        return mTopRowFactory = Mockito.spy(super.createTopKeyboardExtensionFactory());
    }

    @NonNull
    @Override
    protected KeyboardFactory createKeyboardFactory() {
        return mKeyboardFactory = Mockito.spy(super.createKeyboardFactory());
    }

    @NonNull
    @Override
    protected KeyboardThemeFactory createKeyboardThemeFactory() {
        return mThemeFactory = Mockito.spy(super.createKeyboardThemeFactory());
    }

    @NonNull
    @Override
    protected QuickTextKeyFactory createQuickTextKeyFactory() {
        return mQuickKeyFactory = Mockito.spy(super.createQuickTextKeyFactory());
    }

    public ExternalDictionaryFactory getSpiedDictionaryFactory() {
        return mDictionaryFactory;
    }

    public QuickTextKeyFactory getSpiedQuickKeyFactory() {
        return mQuickKeyFactory;
    }

    public KeyboardExtensionFactory getSpiedToolsKeyboardFactory() {
        return mToolsKeyboardFactory;
    }

    public KeyboardExtensionFactory getSpiedBottomRowFactory() {
        return mBottomRowFactory;
    }

    public KeyboardExtensionFactory getSpiedTopRowFactory() {
        return mTopRowFactory;
    }

    public KeyboardFactory getSpiedKeyboardFactory() {
        return mKeyboardFactory;
    }

    public KeyboardThemeFactory getSpiedThemeFactory() {
        return mThemeFactory;
    }
}
