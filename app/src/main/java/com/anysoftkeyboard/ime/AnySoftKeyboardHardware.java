package com.anysoftkeyboard.ime;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.physical.HardKeyboardActionImpl;
import com.anysoftkeyboard.keyboards.physical.MyMetaKeyKeyListener;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardHardware extends AnySoftKeyboardPressEffects {

    private final HardKeyboardActionImpl mHardKeyboardAction = new HardKeyboardActionImpl();
    private long mMetaState;
    private int mLastEditorIdPhysicalKeyboardWasUsed = 0;

    private boolean mUseVolumeKeyForLeftRight;
    private boolean mUseKeyRepeat;
    private boolean mSwitchLanguageOnAltSpace;
    private boolean mSwitchLanguageOnShiftSpace;
    protected boolean mUseBackWord;

    @Override
    public void onCreate() {
        super.onCreate();
        addDisposable(prefs().getBoolean(R.string.settings_key_use_volume_key_for_left_right, R.bool.settings_default_use_volume_key_for_left_right)
                .asObservable().subscribe(aBoolean -> mUseVolumeKeyForLeftRight = aBoolean, GenericOnError.onError("settings_key_use_volume_key_for_left_right")));
        addDisposable(prefs().getBoolean(R.string.settings_key_use_key_repeat, R.bool.settings_default_use_key_repeat)
                .asObservable().subscribe(aBoolean -> mUseKeyRepeat = aBoolean, GenericOnError.onError("settings_key_use_key_repeat")));
        addDisposable(prefs().getBoolean(R.string.settings_key_enable_alt_space_language_shortcut, R.bool.settings_default_enable_alt_space_language_shortcut)
                .asObservable().subscribe(aBoolean -> mSwitchLanguageOnAltSpace = aBoolean, GenericOnError.onError("settings_key_enable_alt_space_language_shortcut")));
        addDisposable(prefs().getBoolean(R.string.settings_key_enable_shift_space_language_shortcut, R.bool.settings_default_enable_shift_space_language_shortcut)
                .asObservable().subscribe(aBoolean -> mSwitchLanguageOnShiftSpace = aBoolean, GenericOnError.onError("settings_key_enable_shift_space_language_shortcut")));
        addDisposable(prefs().getBoolean(R.string.settings_key_use_backword, R.bool.settings_default_use_backword)
                .asObservable().subscribe(aBoolean -> mUseBackWord = aBoolean, GenericOnError.onError("settings_key_use_backword")));
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        //properly finished input. Next time we DO want to show the keyboard view
        mLastEditorIdPhysicalKeyboardWasUsed = 0;
    }

    @Override
    public boolean onShowInputRequested(int flags, boolean configChange) {
        final EditorInfo editorInfo = getCurrentInputEditorInfo();
        //in case the user has used physical keyboard with this input-field,
        //we will not show the keyboard view (until completely finishing, or switching input fields)
        final boolean previouslyPhysicalKeyboardInput;
        if (!configChange && editorInfo != null && editorInfo.fieldId == mLastEditorIdPhysicalKeyboardWasUsed && editorInfo.fieldId != 0) {
            Logger.d(TAG, "Already used physical keyboard on this input-field. Will not show keyboard view.");
            previouslyPhysicalKeyboardInput = true;
        } else {
            previouslyPhysicalKeyboardInput = false;
            mLastEditorIdPhysicalKeyboardWasUsed = 0;
        }
        return !previouslyPhysicalKeyboardInput && super.onShowInputRequested(flags, configChange);
    }

    @Override
    public boolean onKeyDown(final int keyEventKeyCode, @NonNull KeyEvent event) {
        InputConnection ic = getCurrentInputConnection();
        if (handleSelectionExpending(keyEventKeyCode, ic, mGlobalSelectionStartPosition, mGlobalCursorPosition))
            return true;
        final boolean shouldTranslateSpecialKeys = isInputViewShown();

        //greater than zero means it is a physical keyboard.
        //we also want to hide the view if it's a glyph (for example, not physical volume-up key)
        if (event.getDeviceId() > 0 && event.isPrintingKey()) onPhysicalKeyboardKeyPressed();

        mHardKeyboardAction.initializeAction(event, mMetaState);

        switch (keyEventKeyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (shouldTranslateSpecialKeys && mUseVolumeKeyForLeftRight) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                    return true;
                }
                // DO NOT DELAY VOLUME UP KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyEventKeyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (shouldTranslateSpecialKeys && mUseVolumeKeyForLeftRight) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                    return true;
                }
                // DO NOT DELAY VOLUME DOWN KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyEventKeyCode, event);
            /*
             * END of SPECIAL translated HW keys code section
             */
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0 && getInputView() != null && handleCloseRequest()) {
                    // consuming the meta keys
                    if (ic != null) {
                        // translated, so we also take care of the meta-state-keys
                        ic.clearMetaKeyStates(Integer.MAX_VALUE);
                    }
                    mMetaState = 0;
                    return true;
                }
                break;
            case 0x000000cc:// API 14: KeyEvent.KEYCODE_LANGUAGE_SWITCH
                switchToNextPhysicalKeyboard(ic);
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SYM:
                mMetaState = MyMetaKeyKeyListener.handleKeyDown(mMetaState, keyEventKeyCode, event);
                break;
            case KeyEvent.KEYCODE_SPACE:
                if ((event.isAltPressed() && mSwitchLanguageOnAltSpace)
                        || (event.isShiftPressed() && mSwitchLanguageOnShiftSpace)) {
                    switchToNextPhysicalKeyboard(ic);
                    return true;
                }
                // NOTE:
                // letting it fall through to the "default"
            default:

                // Fix issue 185, check if we should process key repeat
                if (!mUseKeyRepeat && event.getRepeatCount() > 0)
                    return true;

                AnyKeyboard.HardKeyboardTranslator keyTranslator = (AnyKeyboard.HardKeyboardTranslator) getCurrentAlphabetKeyboard();
                if (getKeyboardSwitcher().isCurrentKeyboardPhysical() && keyTranslator != null) {
                    // sometimes, the physical keyboard will delete input, and then add some.
                    // we'll try to make it nice.
                    if (ic != null)
                        ic.beginBatchEdit();
                    try {
                        // issue 393, back-word on the hw keyboard!
                        if (mUseBackWord && keyEventKeyCode == KeyEvent.KEYCODE_DEL && event.isShiftPressed()) {
                            handleBackWord(ic);
                            return true;
                        } else {
                            // http://article.gmane.org/gmane.comp.handhelds.openmoko.android-freerunner/629
                            keyTranslator.translatePhysicalCharacter(mHardKeyboardAction, this, mMultiTapTimeout);

                            if (mHardKeyboardAction.getKeyCodeWasChanged()) {
                                final int translatedChar = mHardKeyboardAction.getKeyCode();
                                // typing my own.
                                onKey(translatedChar, null, -1, new int[]{translatedChar}, true/*faking from UI*/);
                                // my handling we are at a regular key press, so we'll update
                                // our meta-state member
                                mMetaState = MyMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                                return true;
                            }
                        }
                    } finally {
                        if (ic != null)
                            ic.endBatchEdit();
                    }
                }
                if (event.isPrintingKey()) {
                    // we are at a regular key press, so we'll update our
                    // meta-state
                    // member
                    mMetaState = MyMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                }
        }
        return super.onKeyDown(keyEventKeyCode, event);
    }

    protected abstract void handleBackWord(InputConnection ic);

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        switch (keyCode) {
            // Issue 248
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!isInputViewShown()) {
                    return super.onKeyUp(keyCode, event);
                }
                if (mUseVolumeKeyForLeftRight) {
                    // without calling super, press-sound will not be played
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (getInputView() != null && getInputView().isShown()
                        && getInputView().isShifted()) {
                    event = new KeyEvent(event.getDownTime(), event.getEventTime(),
                            event.getAction(), event.getKeyCode(),
                            event.getRepeatCount(), event.getDeviceId(),
                            event.getScanCode(), KeyEvent.META_SHIFT_LEFT_ON
                            | KeyEvent.META_SHIFT_ON);
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null)
                        ic.sendKeyEvent(event);

                    return true;
                }
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            case KeyEvent.KEYCODE_SYM:
                mMetaState = MyMetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event);
                setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                break;
            default:
                return super.onKeyUp(keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    private void setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            int clearStatesFlags = 0;
            if (MyMetaKeyKeyListener.getMetaState(mMetaState,
                    MyMetaKeyKeyListener.META_ALT_ON) == 0)
                clearStatesFlags += KeyEvent.META_ALT_ON;
            if (MyMetaKeyKeyListener.getMetaState(mMetaState,
                    MyMetaKeyKeyListener.META_SHIFT_ON) == 0)
                clearStatesFlags += KeyEvent.META_SHIFT_ON;
            if (MyMetaKeyKeyListener.getMetaState(mMetaState,
                    MyMetaKeyKeyListener.META_SYM_ON) == 0)
                clearStatesFlags += KeyEvent.META_SYM_ON;
            ic.clearMetaKeyStates(clearStatesFlags);
        }
    }

    private void switchToNextPhysicalKeyboard(InputConnection ic) {
        // consuming the meta keys
        if (ic != null) {
            // translated, so we also take care of the meta-keys.
            ic.clearMetaKeyStates(Integer.MAX_VALUE);
        }
        mMetaState = 0;
        // only physical keyboard
        getKeyboardSwitcher().nextKeyboard(getCurrentInputEditorInfo(),
                KeyboardSwitcher.NextKeyboardType.AlphabetSupportsPhysical);
    }

    private void onPhysicalKeyboardKeyPressed() {
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        mLastEditorIdPhysicalKeyboardWasUsed = editorInfo == null ? 0 : editorInfo.fieldId;
        if (mHideKeyboardWhenPhysicalKeyboardUsed) {
            hideWindow();
        }

        // For all other keys, if we want to do transformations on
        // text being entered with a hard keyboard, we need to process
        // it and do the appropriate action.
        // using physical keyboard is more annoying with candidate view in
        // the way
        // so we disable it.

        // stopping any soft-keyboard prediction
        abortCorrectionAndResetPredictionState(false);
    }
}
