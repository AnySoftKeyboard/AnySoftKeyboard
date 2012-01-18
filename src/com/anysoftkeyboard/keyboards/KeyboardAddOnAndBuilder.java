package com.anysoftkeyboard.keyboards;

import android.content.Context;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.addons.AddOnImpl;

public class KeyboardAddOnAndBuilder extends AddOnImpl {

	public static final String KEYBOARD_PREF_PREFIX = "keyboard_";
	
	private final int mResId;
    private final int mLandscapeResId;
    private final int mIconResId;
    private final String mDefaultDictionary;
    private final int mQwertyTranslationId;
    private final String mAdditionalIsLetterExceptions;
    private final String mSentenceSeparators;
    private final boolean mKeyboardDefaultEnabled;
    
    public KeyboardAddOnAndBuilder(Context packageContext, String id, int nameResId,
            int layoutResId, int landscapeLayoutResId,
            String defaultDictionary, int iconResId,
            int physicalTranslationResId,
            String additionalIsLetterExceptions,
            String sentenceSeparators,
            String description,
            int keyboardIndex,
            boolean keyboardDefaultEnabled) {
		super(packageContext, KEYBOARD_PREF_PREFIX+id, nameResId, description, keyboardIndex);
		
		mResId = layoutResId;
        if (landscapeLayoutResId == -1){
            mLandscapeResId = mResId;
        } else {
            mLandscapeResId = landscapeLayoutResId;
        }
		
        mDefaultDictionary = defaultDictionary;
        mIconResId = iconResId;
        mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
        mSentenceSeparators = sentenceSeparators;
        mQwertyTranslationId = physicalTranslationResId;
        mKeyboardDefaultEnabled = keyboardDefaultEnabled;
	}
    
    public boolean getKeyboardDefaultEnabled() {
    	return mKeyboardDefaultEnabled;
    }
    
    public String getKeyboardLocale()
    {
    	return mDefaultDictionary;
    }
    
    public AnyKeyboard createKeyboard(AnyKeyboardContextProvider askContext, int mode) {
        return new ExternalAnyKeyboard(askContext, getPackageContext(), mResId, mLandscapeResId, getId(), getNameResId(), mIconResId, mQwertyTranslationId, mDefaultDictionary, mAdditionalIsLetterExceptions, mSentenceSeparators, mode);
    }
}
