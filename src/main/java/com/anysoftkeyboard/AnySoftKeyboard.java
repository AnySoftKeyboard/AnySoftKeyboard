/*
 * Copyright (c) 2015 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.anysoftkeyboard.LayoutSwitchAnimationListener.AnimationType;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.dictionaries.TextEntryState.State;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.keyboards.CondenseType;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher.NextKeyboardType;
import com.anysoftkeyboard.keyboards.physical.HardKeyboardActionImpl;
import com.anysoftkeyboard.keyboards.physical.MyMetaKeyKeyListener;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.receivers.PackagesChangedReceiver;
import com.anysoftkeyboard.receivers.SoundPreferencesChangedReceiver;
import com.anysoftkeyboard.receivers.SoundPreferencesChangedReceiver.SoundPreferencesChangedListener;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.VoiceInputNotInstalledActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.base.utils.GCUtils;
import com.anysoftkeyboard.base.utils.GCUtils.MemRelatedOperation;
import com.anysoftkeyboard.utils.Log;
import com.anysoftkeyboard.utils.ModifierKeyState;
import com.anysoftkeyboard.utils.Workarounds;
import com.google.android.voiceime.VoiceRecognitionTrigger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.FeaturesSet;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Input method implementation for QWERTY-ish keyboard.
 */
public class AnySoftKeyboard extends InputMethodService implements
        OnKeyboardActionListener, OnSharedPreferenceChangeListener,
        AnyKeyboardContextProvider, SoundPreferencesChangedListener {

    private final static String TAG = "ASK";
    private static final long MINIMUM_REFRESH_TIME_FOR_DICTIONARIES = 30 * 1000;
    private static final String KEYBOARD_NOTIFICATION_ALWAYS = "1";
    private static final String KEYBOARD_NOTIFICATION_ON_PHYSICAL = "2";
    private static final String KEYBOARD_NOTIFICATION_NEVER = "3";
    private static final long ONE_FRAME_DELAY = 1000l / 60l;
    private static final long CLOSE_DICTIONARIES_DELAY = 5 * ONE_FRAME_DELAY;

    private final AskPrefs mAskPrefs;
    private final ModifierKeyState mShiftKeyState = new ModifierKeyState(true/*supports locked state*/);
    private final ModifierKeyState mControlKeyState = new ModifierKeyState(false/*does not support locked state*/);
    private final HardKeyboardActionImpl mHardKeyboardAction = new HardKeyboardActionImpl();
    private final KeyboardUIStateHandler mKeyboardHandler = new KeyboardUIStateHandler(this);

    // receive ringer mode changes to detect silent mode
    private final SoundPreferencesChangedReceiver mSoundPreferencesChangedReceiver = new SoundPreferencesChangedReceiver(this);
    private final PackagesChangedReceiver mPackagesChangedReceiver = new PackagesChangedReceiver(this);
    protected IBinder mImeToken = null;
    KeyboardSwitcher mKeyboardSwitcher;
    /*package*/ TextView mCandidateCloseText;
    private SharedPreferences mPrefs;
    private LayoutSwitchAnimationListener mSwitchAnimator;
    private boolean mDistinctMultiTouch = true;
    private AnyKeyboardView mInputView;
    private View mCandidatesParent;
    private CandidateView mCandidateView;
    private long mLastDictionaryRefresh = -1;
    private int mMinimumWordCorrectionLength = 2;
    private Suggest mSuggest;
    private CompletionInfo[] mCompletions;
    private AlertDialog mOptionsDialog;
    private long mMetaState;
    private Set<Character> mSentenceSeparators = Collections.emptySet();
    // private BTreeDictionary mContactsDictionary;
    private EditableDictionary mUserDictionary;
    private AutoDictionary mAutoDictionary;
    private WordComposer mWord = new WordComposer();
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mCommittedLength;
    private CharSequence mCommittedWord = "";
    /*
     * Do we do prediction now
     */
    private boolean mPredicting;
    /*
     * is prediction needed for the current input connection
     */
    private boolean mPredictionOn;
    /*
     * is out-side completions needed
     */
    private boolean mCompletionOn;
    private boolean mAutoSpace;
    private boolean mAutoCorrectOn;
    private boolean mAllowSuggestionsRestart = true;
    private boolean mCurrentlyAllowSuggestionRestart = true;
    private boolean mJustAutoAddedWord = false;
    private boolean mDoNotFlipQuickTextKeyAndPopupFunctionality;
    private String mOverrideQuickTextText = null;
    private boolean mAutoCap;
    private boolean mQuickFixes;
    private int mCalculatedCommonalityMaxLengthDiff;
    private int mCalculatedCommonalityMaxDistance;
    /*
     * Configuration flag. Should we support dictionary suggestions
     */
    private boolean mShowSuggestions = false;
    private boolean mAutoComplete;
    // private int mCorrectionMode;
    private String mKeyboardChangeNotificationType;
    /*
     * This will help us find out if UNDO_COMMIT is still possible to be done
     */
    private int mUndoCommitCursorPosition = -2;
    private AudioManager mAudioManager;
    private boolean mSilentMode;
    private boolean mSoundOn;
    // between 0..100. This is the custom volume
    private int mSoundVolume;
    private Vibrator mVibrator;
    private int mVibrationDuration;
    private CondenseType mKeyboardInCondensedMode = CondenseType.None;
    private boolean mJustAddedAutoSpace;
    private CharSequence mJustAddOnText = null;
    private boolean mLastCharacterWasShifted = false;
    private InputMethodManager mInputMethodManager;
    private VoiceRecognitionTrigger mVoiceRecognitionTrigger;

    public AnySoftKeyboard() {
        mAskPrefs = AnyApplication.getConfig();
    }

    private static int getCursorPosition(InputConnection connection) {
        if (connection == null)
            return 0;
        ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted == null)
            return 0;
        return extracted.startOffset + extracted.selectionStart;
    }

    private static boolean isBackWordStopChar(int c) {
        return !Character.isLetter(c);
    }

    private static String getDictionaryOverrideKey(AnyKeyboard currentKeyboard) {
        return currentKeyboard.getKeyboardPrefId() + "_override_dictionary";
    }

    @Override
    @NonNull
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl() {
            @Override
            public void attachToken(IBinder token) {
                super.attachToken(token);
                mImeToken = token;
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (DeveloperUtils.hasTracingRequested(getApplicationContext())) {
            try {
                DeveloperUtils.startTracing();
                Toast.makeText(getApplicationContext(),
                        R.string.debug_tracing_starting, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //see issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/105
                //I might get a "Permission denied" error.
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        R.string.debug_tracing_starting_failed, Toast.LENGTH_LONG).show();
            }
        }
        Log.i(TAG, "****** AnySoftKeyboard v%s (%d) service started.", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        if (!BuildConfig.DEBUG && BuildConfig.VERSION_NAME.endsWith("-SNAPSHOT"))
            throw new RuntimeException("You can not run a 'RELEASE' build with a SNAPSHOT postfix!");

        if (mAskPrefs.getAnimationsLevel() != AskPrefs.AnimationsLevel.None) {
            final int fancyAnimation = getResources().getIdentifier("Animation_InputMethodFancy", "style", "android");
            if (fancyAnimation != 0) {
                Log.i(TAG, "Found Animation_InputMethodFancy as %d, so I'll use this", fancyAnimation);
                getWindow().getWindow().setWindowAnimations(fancyAnimation);
            } else {
                Log.w(TAG, "Could not find Animation_InputMethodFancy, using default animation");
                getWindow().getWindow().setWindowAnimations(android.R.style.Animation_InputMethod);
            }
        }

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateRingerMode();
        // register to receive ringer mode changes for silent mode
        registerReceiver(mSoundPreferencesChangedReceiver,
                mSoundPreferencesChangedReceiver.createFilterToRegisterOn());
        // register to receive packages changes
        registerReceiver(mPackagesChangedReceiver,
                mPackagesChangedReceiver.createFilterToRegisterOn());
        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
        loadSettings();
        mAskPrefs.addChangedListener(this);
        mKeyboardSwitcher = new KeyboardSwitcher(this);

        mOrientation = getResources().getConfiguration().orientation;

        mSentenceSeparators = getCurrentKeyboard().getSentenceSeparators();

        if (mSuggest == null) {
            initSuggest();
        }

        if (mKeyboardChangeNotificationType
                .equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
            notifyKeyboardChangeIfNeeded();
        }

        mVoiceRecognitionTrigger = new VoiceRecognitionTrigger(this);

        mSwitchAnimator = new LayoutSwitchAnimationListener(this);
    }

    private void initSuggest() {
        mSuggest = new Suggest(this);
        mSuggest.setCorrectionMode(mQuickFixes, mShowSuggestions, mCalculatedCommonalityMaxLengthDiff, mCalculatedCommonalityMaxDistance);
        mSuggest.setMinimumWordLengthForCorrection(mMinimumWordCorrectionLength);
        setDictionariesForCurrentKeyboard();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "AnySoftKeyboard has been destroyed! Cleaning resources..");
        mSwitchAnimator.onDestory();
        mKeyboardHandler.removeAllMessages();
        mAskPrefs.removeChangedListener(this);

        unregisterReceiver(mSoundPreferencesChangedReceiver);
        unregisterReceiver(mPackagesChangedReceiver);

        mInputMethodManager.hideStatusIcon(mImeToken);

        if (mInputView != null) mInputView.onViewNotRequired();
        mInputView = null;

        mKeyboardSwitcher.setInputView(null);

        closeDictionaries();

        if (DeveloperUtils.hasTracingStarted()) {
            DeveloperUtils.stopTracing();
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.debug_tracing_finished,
                            DeveloperUtils.getTraceFile()), Toast.LENGTH_SHORT)
                    .show();
        }

        super.onDestroy();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);

        if (!mKeyboardChangeNotificationType
                .equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
            mInputMethodManager.hideStatusIcon(mImeToken);
        }
        // Remove pending messages related to update suggestions
        abortCorrection(true, false);
    }

    AnyKeyboardView getInputView() {
        return mInputView;
    }

    @Override
    public void setInputView(@NonNull View view) {
        super.setInputView(view);
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            // this is required for animations, so the background will be
            // consist.
            ((View) parent).setBackgroundResource(R.drawable.ask_wallpaper);
        } else {
            Log.w(TAG,
                    "*** It seams that the InputView parent is not a View!! This is very strange.");
        }
    }

    @Override
    public View onCreateInputView() {
        if (mInputView != null)
            mInputView.onViewNotRequired();
        mInputView = null;

        GCUtils.getInstance().performOperationWithMemRetry(TAG,
                new MemRelatedOperation() {
                    public void operation() {
                        mInputView = (AnyKeyboardView) getLayoutInflater().inflate(R.layout.main_keyboard_layout, null);
                    }
                }, true);
        // resetting token users
        mOptionsDialog = null;

        mKeyboardSwitcher.setInputView(mInputView);
        mInputView.setOnKeyboardActionListener(this);

        mDistinctMultiTouch = mInputView.hasDistinctMultitouch();

        return mInputView;
    }

    @Override
    public View onCreateCandidatesView() {
        mKeyboardSwitcher.makeKeyboards(false);
        final ViewGroup candidateViewContainer = (ViewGroup) getLayoutInflater().inflate(R.layout.candidates, null);
        mCandidatesParent = null;
        mCandidateView = (CandidateView) candidateViewContainer.findViewById(R.id.candidates);
        mCandidateView.setService(this);
        setCandidatesViewShown(false);

        final KeyboardTheme theme = KeyboardThemeFactory
                .getCurrentKeyboardTheme(getApplicationContext());
        final TypedArray a = theme.getPackageContext().obtainStyledAttributes(
                null, R.styleable.AnyKeyboardViewTheme, 0,
                theme.getThemeResId());
        int closeTextColor = ContextCompat.getColor(this, R.color.candidate_other);
        float fontSizePixel = getResources().getDimensionPixelSize(
                R.dimen.candidate_font_height);
        try {
            closeTextColor = a.getColor(
                    R.styleable.AnyKeyboardViewTheme_suggestionOthersTextColor,
                    closeTextColor);
            fontSizePixel = a.getDimension(
                    R.styleable.AnyKeyboardViewTheme_suggestionTextSize,
                    fontSizePixel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.recycle();

        mCandidateCloseText = (TextView) candidateViewContainer.findViewById(R.id.close_suggestions_strip_text);
        View closeIcon = candidateViewContainer.findViewById(R.id.close_suggestions_strip_icon);

        if (mCandidateCloseText != null && closeIcon != null) {// why? In API3
            // it is not supported
            closeIcon.setOnClickListener(new OnClickListener() {
                // two seconds is enough.
                private final static long DOUBLE_TAP_TIMEOUT = 2 * 1000;

                public void onClick(View v) {
                    mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
                    mCandidateCloseText.setVisibility(View.VISIBLE);
                    mCandidateCloseText.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.close_candidates_hint_in));
                    mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT), DOUBLE_TAP_TIMEOUT - 50);
                }
            });

            mCandidateCloseText.setTextColor(closeTextColor);
            mCandidateCloseText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    fontSizePixel);
            mCandidateCloseText.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
                    mCandidateCloseText.setVisibility(View.GONE);
                    abortCorrection(true, true);
                }
            });
        }

        return candidateViewContainer;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        Log.d(TAG, "onStartInput(EditorInfo:" + attribute.imeOptions + ","
                + attribute.inputType + ", restarting:" + restarting + ")");
        super.onStartInput(attribute, restarting);
        //removing close request (if it was asked for a previous onFinishInput).
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES);

        abortCorrection(true, false);

        if (!restarting) {
            TextEntryState.newSession(this);
            // Clear shift states.
            mMetaState = 0;
            mCurrentlyAllowSuggestionRestart = mAllowSuggestionsRestart;
        } else {
            // something very fishy happening here...
            // this is the only way I can get around it.
            // it seems that when a onStartInput is called with restarting ==
            // true
            // suggestions restart fails :(
            // see Browser when editing multiline textbox
            mCurrentlyAllowSuggestionRestart = false;
        }
    }

    @Override
    public void onStartInputView(final EditorInfo attribute,
                                 final boolean restarting) {
        Log.d(TAG, "onStartInputView(EditorInfo{imeOptions %d, inputType %d}, restarting %s",
                attribute.imeOptions, attribute.inputType, restarting);

        super.onStartInputView(attribute, restarting);
        if (mVoiceRecognitionTrigger != null) {
            mVoiceRecognitionTrigger.onStartInputView();
        }

        if (mInputView == null) {
            return;
        }

        mInputView.dismissPopupKeyboard();
        mInputView.setKeyboardActionType(attribute.imeOptions);
        mKeyboardSwitcher.makeKeyboards(false);

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_DATETIME:
                Log.d(TAG, "Setting MODE_DATETIME as keyboard due to a TYPE_CLASS_DATETIME input.");
                mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_DATETIME, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_NUMBER:
                Log.d(TAG, "Setting MODE_NUMBERS as keyboard due to a TYPE_CLASS_NUMBER input.");
                mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NUMBERS, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_PHONE:
                Log.d(TAG, "Setting MODE_PHONE as keyboard due to a TYPE_CLASS_PHONE input.");
                mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_PHONE, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_TEXT:
                Log.d(TAG, "A TYPE_CLASS_TEXT input.");
                final int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
                switch (variation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        Log.d(TAG, "A password TYPE_CLASS_TEXT input with no prediction");
                        mPredictionOn = false;
                        break;
                    default:
                        mPredictionOn = true;
                }

                if (mAskPrefs.getInsertSpaceAfterCandidatePick()) {
                    switch (variation) {
                        case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                        case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                            mAutoSpace = false;
                            break;
                        default:
                            mAutoSpace = true;
                    }
                } else {
                    // some users don't want auto-space
                    mAutoSpace = false;
                }

                switch (variation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        Log.d(TAG, "Setting MODE_EMAIL as keyboard due to a TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
                        mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_EMAIL, attribute, restarting);
                        mPredictionOn = false;
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        Log.d(TAG, "Setting MODE_URL as keyboard due to a TYPE_TEXT_VARIATION_URI input.");
                        mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_URL, attribute, restarting);
                        mPredictionOn = false;
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
                        Log.d(TAG, "Setting MODE_IM as keyboard due to a TYPE_TEXT_VARIATION_SHORT_MESSAGE input.");
                        mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_IM, attribute, restarting);
                        break;
                    default:
                        Log.d(TAG, "Setting MODE_TEXT as keyboard due to a default input.");
                        mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute, restarting);
                }

                final int textFlag = attribute.inputType & EditorInfo.TYPE_MASK_FLAGS;
                if ((textFlag & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS ||
                        (textFlag & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) == EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) {
                        Log.d(TAG, "Input requested NO_SUGGESTIONS, or it is AUTO_COMPLETE by itself.");
                        mPredictionOn = false;
                }

                break;
            default:
                Log.d(TAG, "Setting MODE_TEXT as keyboard due to a default input.");
                // No class. Probably a console window, or no GUI input connection
                mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute, restarting);
                mPredictionOn = false;
                mAutoSpace = true;
        }

        mPredicting = false;
        mJustAddedAutoSpace = false;
        setCandidatesViewShown(false);

        if (mSuggest != null) {
            mSuggest.setCorrectionMode(mQuickFixes, mShowSuggestions, mCalculatedCommonalityMaxLengthDiff, mCalculatedCommonalityMaxDistance);
        }

        mPredictionOn = mPredictionOn && (mShowSuggestions/* || mQuickFixes */);

        clearSuggestions();

        if (mPredictionOn) {
            if ((SystemClock.elapsedRealtime() - mLastDictionaryRefresh) > MINIMUM_REFRESH_TIME_FOR_DICTIONARIES)
                setDictionariesForCurrentKeyboard();
        } else {
            // this will release memory
            setDictionariesForCurrentKeyboard();
        }

        updateShiftStateNow();
    }

    @Override
    public void hideWindow() {
        if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
            mOptionsDialog.dismiss();
            mOptionsDialog = null;
        }

        super.hideWindow();

        TextEntryState.endSession();
    }

    @Override
    public void onFinishInput() {
        Log.d(TAG, "onFinishInput()");
        super.onFinishInput();

        if (mInputView != null) {
            mInputView.closing();
        }

        if (!mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
            mInputMethodManager.hideStatusIcon(mImeToken);
        }
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES, CLOSE_DICTIONARIES_DELAY);
    }

    /*
     * this function is called EVERY TIME them selection is changed. This also
     * includes the underlined suggestions.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd, int candidatesStart,
                                  int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        Log.d(TAG, "onUpdateSelection: oss=" + oldSelStart + ", ose="
                + oldSelEnd + ", nss=" + newSelStart + ", nse=" + newSelEnd
                + ", cs=" + candidatesStart + ", ce=" + candidatesEnd);
        //next UI thread loop, please recalculate the shift state

        updateShiftStateNow();

        mWord.setGlobalCursorPosition(newSelEnd);

        if (!isPredictionOn()) {
            return;// not relevant if no prediction is needed.
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;// well, I can't do anything without this connection

        Log.d(TAG, "onUpdateSelection: ok, let's see what can be done");

        if (newSelStart != newSelEnd) {
            // text selection. can't predict in this mode
            Log.d(TAG, "onUpdateSelection: text selection.");
            abortCorrection(true, false);
        } else {
            // we have the following options (we are in an input which requires
            // predicting (mPredictionOn == true):
            // 1) predicting and moved inside the word
            // 2) predicting and moved outside the word
            // 2.1) to a new word
            // 2.2) to no word land
            // 3) not predicting
            // 3.1) to a new word
            // 3.2) to no word land

            // so, 1 and 2 requires that predicting is currently done, and the
            // cursor moved
            if (mPredicting) {
                if (newSelStart >= candidatesStart && newSelStart <= candidatesEnd) {
                    // 1) predicting and moved inside the word - just update the
                    // cursor position and shift state
                    // inside the currently selected word
                    int cursorPosition = newSelEnd - candidatesStart;
                    if (mWord.setCursorPosition(cursorPosition)) {
                        Log.d(TAG, "onUpdateSelection: cursor moving inside the predicting word");
                    }
                } else {
                    Log.d(TAG, "onUpdateSelection: cursor moving outside the currently predicting word");
                    abortCorrection(true, false);
                    // ask user whether to restart
                    postRestartWordSuggestion();
                }
            } else {
                Log.d(TAG,
                        "onUpdateSelection: not predicting at this moment, maybe the cursor is now at a new word?");
                if (TextEntryState.getState() == State.ACCEPTED_DEFAULT) {
                    if (mUndoCommitCursorPosition == oldSelStart && mUndoCommitCursorPosition != newSelStart) {
                        Log.d(TAG, "onUpdateSelection: I am in ACCEPTED_DEFAULT state, but the user moved the cursor, so it is not possible to undo_commit now.");
                        abortCorrection(true, false);
                    } else if (mUndoCommitCursorPosition == -2) {
                        Log.d(TAG, "onUpdateSelection: I am in ACCEPTED_DEFAULT state, time to store the position - I can only undo-commit from here.");
                        mUndoCommitCursorPosition = newSelStart;
                    }
                }
                postRestartWordSuggestion();
            }
        }
    }

    private void postRestartWordSuggestion() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }

    private boolean canRestartWordSuggestion() {
        if (mPredicting || !isPredictionOn() || !mAllowSuggestionsRestart
                || !mCurrentlyAllowSuggestionRestart || mInputView == null
                || !mInputView.isShown()) {
            // why?
            // mPredicting - if I'm predicting a word, I can not restart it..
            // right? I'm inside that word!
            // isPredictionOn() - this is obvious.
            // mAllowSuggestionsRestart - config settings
            // mCurrentlyAllowSuggestionRestart - workaround for
            // onInputStart(restarting == true)
            // mInputView == null - obvious, no?
            Log.d(TAG, "performRestartWordSuggestion: no need to restart: mPredicting=%s, isPredictionOn=%s, mAllowSuggestionsRestart=%s, mCurrentlyAllowSuggestionRestart=%s"
                    , mPredicting, isPredictionOn(), mAllowSuggestionsRestart, mCurrentlyAllowSuggestionRestart);
            return false;
        } else if (!isCursorTouchingWord()) {
            Log.d(TAG, "User moved cursor to no-man land. Bye bye.");
            return false;
        }

        return true;
    }

    public void performRestartWordSuggestion(final InputConnection ic) {
        // I assume ASK DOES NOT predict at this moment!

        // 2) predicting and moved outside the word - abort predicting, update
        // shift state
        // 2.1) to a new word - restart predicting on the new word
        // 2.2) to no word land - nothing else

        // this means that the new cursor position is outside the candidates
        // underline
        // this can be either because the cursor is really outside the
        // previously underlined (suggested)
        // or nothing was suggested.
        // in this case, we would like to reset the prediction and restart
        // if the user clicked inside a different word
        // restart required?
        if (canRestartWordSuggestion()) {// 2.1
            ic.beginBatchEdit();// don't want any events till I finish handling
            // this touch
            Log.d(TAG,
                    "User moved cursor to a word. Should I restart predition?");
            abortCorrection(true, false);

            // locating the word
            CharSequence toLeft = "";
            CharSequence toRight = "";
            while (true) {
                Log.d(TAG, "Checking left offset " + toLeft.length()
                        + ". Currently have '" + toLeft + "'");
                CharSequence newToLeft = ic.getTextBeforeCursor(
                        toLeft.length() + 1, 0);
                if (TextUtils.isEmpty(newToLeft)
                        || isWordSeparator(newToLeft.charAt(0))
                        || newToLeft.length() == toLeft.length()) {
                    break;
                }
                toLeft = newToLeft;
            }
            while (true) {
                Log.d(TAG, "Checking right offset " + toRight.length()
                        + ". Currently have '" + toRight + "'");
                CharSequence newToRight = ic.getTextAfterCursor(
                        toRight.length() + 1, 0);
                if (TextUtils.isEmpty(newToRight)
                        || isWordSeparator(newToRight.charAt(newToRight
                        .length() - 1))
                        || newToRight.length() == toRight.length()) {
                    break;
                }
                toRight = newToRight;
            }
            CharSequence word = toLeft.toString() + toRight.toString();
            Log.d(TAG, "Starting new prediction on word '" + word + "'.");
            mPredicting = word.length() > 0;
            mUndoCommitCursorPosition = -2;// so it will be marked the next time
            mWord.reset();

            final int[] tempNearByKeys = new int[1];

            for (int index = 0; index < word.length(); index++) {
                final char c = word.charAt(index);
                if (index == 0)
                    mWord.setFirstCharCapitalized(Character.isUpperCase(c));

                tempNearByKeys[0] = c;
                mWord.add(c, tempNearByKeys);

                TextEntryState.typedCharacter(c, false);
            }
            ic.deleteSurroundingText(toLeft.length(), toRight.length());
            ic.setComposingText(word, 1);
            // repositioning the cursor
            if (toRight.length() > 0) {
                final int cursorPosition = getCursorPosition(ic)
                        - toRight.length();
                Log.d(TAG,
                        "Repositioning the cursor inside the word to position "
                                + cursorPosition);
                ic.setSelection(cursorPosition, cursorPosition);
            }

            mWord.setCursorPosition(toLeft.length());
            ic.endBatchEdit();
            postUpdateSuggestions();
        } else {
            Log.d(TAG, "performRestartWordSuggestion canRestartWordSuggestion == false");
        }
    }

    private void onPhysicalKeyboardKeyPressed() {
        if (mAskPrefs.hideSoftKeyboardWhenPhysicalKeyPressed())
            hideWindow();

        // For all other keys, if we want to do transformations on
        // text being entered with a hard keyboard, we need to process
        // it and do the appropriate action.
        // using physical keyboard is more annoying with candidate view in
        // the way
        // so we disable it.

        // to clear the underline.
        abortCorrection(true, false);
    }

    @Override
    public void onComputeInsets(@NonNull InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (FeaturesSet.DEBUG_LOG) {
            Log.d(TAG, "Received completions:");
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                Log.d(TAG, "  #" + i + ": " + completions[i]);
            }
        }

        // completions should be shown if dictionary requires, or if we are in
        // full-screen and have outside completions
        if (mCompletionOn || (isFullscreenMode() && (completions != null))) {
            Log.v(TAG, "Received completions: completion should be shown: "
                    + mCompletionOn + " fullscreen:" + isFullscreenMode());
            mCompletions = completions;
            // we do completions :)

            mCompletionOn = true;
            if (completions == null) {
                Log.v(TAG, "Received completions: completion is NULL. Clearing suggestions.");
                clearSuggestions();
                return;
            }

            List<CharSequence> stringList = new ArrayList<>();
            for (CompletionInfo ci : completions) {
                if (ci != null) stringList.add(ci.getText());
            }
            Log.v(TAG, "Received completions: setting to suggestions view "
                    + stringList.size() + " completions.");
            // CharSequence typedWord = mWord.getTypedWord();
            setSuggestions(stringList, true, true, true);
            mWord.setPreferredWord(null);
            // I mean, if I'm here, it must be shown...
            setCandidatesViewShown(true);
        } else {
            Log.v(TAG, "Received completions: completions should not be shown.");
        }
    }

    @Override
    public void setCandidatesViewShown(boolean shown) {
        // we show predication only in on-screen keyboard
        // (onEvaluateInputViewShown)
        // or if the physical keyboard supports candidates
        // (mPredictionLandscape)
        final boolean shouldShow = shouldCandidatesStripBeShown() && shown;
        final boolean currentlyShown = mCandidatesParent != null && mCandidatesParent.getVisibility() == View.VISIBLE;
        super.setCandidatesViewShown(shouldShow);
        if (shouldShow != currentlyShown) {
            // I believe (can't confirm it) that candidates animation is kinda rare,
            // and it is better to load it on demand, then to keep it in memory always..
            if (shouldShow) {
                mCandidatesParent.setAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.candidates_bottom_to_up_enter));
            } else {
                mCandidatesParent.setAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.candidates_up_to_bottom_exit));
            }
        }
    }

    @Override
    public void setCandidatesView(@NonNull View view) {
        super.setCandidatesView(view);
        mCandidatesParent = view.getParent() instanceof View ? (View) view.getParent() : null;
    }

    private void clearSuggestions() {
        setSuggestions(null, false, false, false);
    }

    private void setSuggestions(List<CharSequence> suggestions,
                                boolean completions, boolean typedWordValid,
                                boolean haveMinimalSuggestion) {
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions,
                    typedWordValid, haveMinimalSuggestion && mAutoCorrectOn);
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        if (getCurrentInputEditorInfo() != null) {
            final EditorInfo editorInfo = getCurrentInputEditorInfo();
            if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_FULLSCREEN) != 0) {
                //if the view DOES NOT want fullscreen, then do what it wants
                Log.d(TAG, "Will not go to Fullscreen because input view requested IME_FLAG_NO_FULLSCREEN");
                return false;
            } else if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0) {
                Log.d(TAG, "Will not go to Fullscreen because input view requested IME_FLAG_NO_EXTRACT_UI");
                return false;

            }
        }

        switch (mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return mAskPrefs.getUseFullScreenInputInLandscape();
            default:
                return mAskPrefs.getUseFullScreenInputInPortrait();
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, @NonNull KeyEvent event) {
        final boolean shouldTranslateSpecialKeys = isInputViewShown();

        if (event.isPrintingKey()) onPhysicalKeyboardKeyPressed();

        mHardKeyboardAction.initializeAction(event, mMetaState);

        InputConnection ic = getCurrentInputConnection();

        switch (keyCode) {
            /****
             * SPECIAL translated HW keys If you add new keys here, do not forget
             * to add to the
             */
            case KeyEvent.KEYCODE_CAMERA:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useCameraKeyForBackspaceBackword()) {
                    handleBackWord(getCurrentInputConnection());
                    return true;
                }
                // DO NOT DELAY CAMERA KEY with unneeded checks in default mark
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_FOCUS:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useCameraKeyForBackspaceBackword()) {
                    handleDeleteLastCharacter(false);
                    return true;
                }
                // DO NOT DELAY FOCUS KEY with unneeded checks in default mark
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useVolumeKeyForLeftRight()) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                    return true;
                }
                // DO NOT DELAY VOLUME UP KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useVolumeKeyForLeftRight()) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                    return true;
                }
                // DO NOT DELAY VOLUME DOWN KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyCode, event);
            /****
             * END of SPECIAL translated HW keys code section
             */
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        // consuming the meta keys
                        if (ic != null) {
                            // translated, so we also take care of the metakeys
                            ic.clearMetaKeyStates(Integer.MAX_VALUE);
                        }
                        mMetaState = 0;
                        return true;
                    }
                }
                break;
            case 0x000000cc:// API 14: KeyEvent.KEYCODE_LANGUAGE_SWITCH
                switchToNextPhysicalKeyboard(ic);
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (event.isAltPressed()
                        && Workarounds.isAltSpaceLangSwitchNotPossible()) {
                    switchToNextPhysicalKeyboard(ic);
                    return true;
                }
                // NOTE: letting it fall-through to the other meta-keys
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SYM:
                Log.d(TAG + "-meta-key",
                        getMetaKeysStates("onKeyDown before handle"));
                mMetaState = MyMetaKeyKeyListener.handleKeyDown(mMetaState,
                        keyCode, event);
                Log.d(TAG + "-meta-key",
                        getMetaKeysStates("onKeyDown after handle"));
                break;
            case KeyEvent.KEYCODE_SPACE:
                if ((event.isAltPressed() && !Workarounds
                        .isAltSpaceLangSwitchNotPossible())
                        || event.isShiftPressed()) {
                    switchToNextPhysicalKeyboard(ic);
                    return true;
                }
                // NOTE:
                // letting it fall through to the "default"
            default:

                // Fix issue 185, check if we should process key repeat
                if (!mAskPrefs.getUseRepeatingKeys() && event.getRepeatCount() > 0)
                    return true;

                if (mKeyboardSwitcher.isCurrentKeyboardPhysical()) {
                    // sometimes, the physical keyboard will delete input, and
                    // then
                    // add some.
                    // we'll try to make it nice
                    if (ic != null)
                        ic.beginBatchEdit();
                    try {
                        // issue 393, backword on the hw keyboard!
                        if (mAskPrefs.useBackword()
                                && keyCode == KeyEvent.KEYCODE_DEL
                                && event.isShiftPressed()) {
                            handleBackWord(ic);
                            return true;
                        } else/* if (event.isPrintingKey()) */ {
                            // http://article.gmane.org/gmane.comp.handhelds.openmoko.android-freerunner/629
                            AnyKeyboard current = mKeyboardSwitcher
                                    .getCurrentKeyboard();

                            HardKeyboardTranslator keyTranslator = (HardKeyboardTranslator) current;

                            if (BuildConfig.DEBUG) {
                                final String keyboardName = current
                                        .getKeyboardName();

                                Log.d(TAG, "Asking '" + keyboardName
                                        + "' to translate key: " + keyCode);
                                Log.v(TAG,
                                        "Hard Keyboard Action before translation: Shift: "
                                                + mHardKeyboardAction
                                                .isShiftActive()
                                                + ", Alt: "
                                                + mHardKeyboardAction.isAltActive()
                                                + ", Key code: "
                                                + mHardKeyboardAction.getKeyCode()
                                                + ", changed: "
                                                + mHardKeyboardAction
                                                .getKeyCodeWasChanged());
                            }

                            keyTranslator.translatePhysicalCharacter(
                                    mHardKeyboardAction, this);

                            Log.v(TAG,
                                    "Hard Keyboard Action after translation: Key code: "
                                            + mHardKeyboardAction.getKeyCode()
                                            + ", changed: "
                                            + mHardKeyboardAction
                                            .getKeyCodeWasChanged());
                            if (mHardKeyboardAction.getKeyCodeWasChanged()) {
                                final int translatedChar = mHardKeyboardAction.getKeyCode();
                                // typing my own.
                                onKey(translatedChar, null, -1, new int[]{translatedChar}, true/*faking from UI*/);
                                // my handling we are at a regular key press, so we'll update
                                // our meta-state member
                                mMetaState = MyMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                                Log.d(TAG + "-meta-key", getMetaKeysStates("onKeyDown after adjust - translated"));
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
                    mMetaState = MyMetaKeyKeyListener
                            .adjustMetaAfterKeypress(mMetaState);
                    Log.d(TAG + "-meta-key",
                            getMetaKeysStates("onKeyDown after adjust"));
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void switchToNextPhysicalKeyboard(InputConnection ic) {
        // consuming the meta keys
        if (ic != null) {
            ic.clearMetaKeyStates(Integer.MAX_VALUE);// translated, so
            // we also take
            // care of the
            // metakeys.
        }
        mMetaState = 0;
        // only physical keyboard
        nextKeyboard(getCurrentInputEditorInfo(),
                NextKeyboardType.AlphabetSupportsPhysical);
    }

    private void notifyKeyboardChangeIfNeeded() {
        // Log.d("anySoftKeyboard","notifyKeyboardChangeIfNeeded");
        // Thread.dumpStack();
        if (mKeyboardSwitcher == null)// happens on first onCreate.
            return;

        if ((mKeyboardSwitcher.isAlphabetMode())
                && !mKeyboardChangeNotificationType
                .equals(KEYBOARD_NOTIFICATION_NEVER)) {
            mInputMethodManager.showStatusIcon(mImeToken, getCurrentKeyboard()
                            .getKeyboardContext().getPackageName(),
                    getCurrentKeyboard().getKeyboardIconResId());
        }
    }

    public AnyKeyboard getCurrentKeyboard() {
        return mKeyboardSwitcher.getCurrentKeyboard();
    }

    public KeyboardSwitcher getKeyboardSwitcher() {
        return mKeyboardSwitcher;
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        Log.d(TAG, "onKeyUp keycode=%d", keyCode);
        switch (keyCode) {
            // Issue 248
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!isInputViewShown()) {
                    return super.onKeyUp(keyCode, event);
                }
                if (mAskPrefs.useVolumeKeyForLeftRight()) {
                    // no need of vol up/down sound
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mInputView != null && mInputView.isShown()
                        && mInputView.isShifted()) {
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
                Log.d("AnySoftKeyboard-meta-key", getMetaKeysStates("onKeyUp"));
                setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private String getMetaKeysStates(String place) {
        final int shiftState = MyMetaKeyKeyListener.getMetaState(mMetaState,
                MyMetaKeyKeyListener.META_SHIFT_ON);
        final int altState = MyMetaKeyKeyListener.getMetaState(mMetaState,
                MyMetaKeyKeyListener.META_ALT_ON);
        final int symState = MyMetaKeyKeyListener.getMetaState(mMetaState,
                MyMetaKeyKeyListener.META_SYM_ON);

        return "Meta keys state at " + place + "- SHIFT:" + shiftState
                + ", ALT:" + altState + " SYM:" + symState + " bits:"
                + MyMetaKeyKeyListener.getMetaState(mMetaState) + " state:"
                + mMetaState;
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
            Log.d("AnySoftKeyboard-meta-key",
                    getMetaKeysStates("setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState with flags: "
                            + clearStatesFlags));
            ic.clearMetaKeyStates(clearStatesFlags);
        }
    }

    private boolean addToDictionaries(WordComposer suggestion,
                                      AutoDictionary.AdditionType type) {
        final boolean added = checkAddToDictionary(suggestion, type);
        if (added) {
            Log.i(TAG, "Word '%s' was added to the auto-dictionary.", suggestion);
        }
        return added;
    }

    /**
     * Adds to the UserBigramDictionary and/or AutoDictionary
     */
    private boolean checkAddToDictionary(WordComposer suggestion,
                                         AutoDictionary.AdditionType type) {
        if (suggestion == null || suggestion.length() < 1)
            return false;
        // Only auto-add to dictionary if auto-correct is ON. Otherwise we'll be
        // adding words in situations where the user or application really
        // didn't
        // want corrections enabled or learned.
        if (!mQuickFixes && !mShowSuggestions)
            return false;

        if (mAutoDictionary != null) {
            String suggestionToCheck = suggestion.getTypedWord().toString();
            if (!mSuggest.isValidWord(suggestionToCheck)) {

                final boolean added = mAutoDictionary.addWord(suggestion, type, this);
                if (added && mCandidateView != null) {
                    mCandidateView.notifyAboutWordAdded(suggestion.getTypedWord());
                }
                return added;
            }
        }
        return false;
    }

    private void commitTyped(InputConnection inputConnection) {
        if (mPredicting) {
            mPredicting = false;
            if (mWord.length() > 0) {
                if (inputConnection != null) {
                    inputConnection.commitText(mWord.getTypedWord(), 1);
                }
                mCommittedLength = mWord.length();
                mCommittedWord = mWord.getTypedWord();
                TextEntryState.acceptedTyped(mWord.getTypedWord());
                addToDictionaries(mWord, AutoDictionary.AdditionType.Typed);
            }
            if (mKeyboardHandler.hasMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS)) {
                postUpdateSuggestions(-1);
            }
        }
    }

    private void swapPunctuationAndSpace() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (!mAskPrefs.shouldSwapPunctuationAndSpace())
            return;
        CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);
        if (BuildConfig.DEBUG) {
            String seps = "";
            for (Character c : mSentenceSeparators)
                seps += c;
            Log.d(TAG, "swapPunctuationAndSpace: lastTwo: '" + lastTwo
                    + "', mSentenceSeparators " + mSentenceSeparators.size()
                    + " '" + seps + "'");
        }
        if (lastTwo != null && lastTwo.length() == 2
                && lastTwo.charAt(0) == KeyCodes.SPACE
                && mSentenceSeparators.contains(lastTwo.charAt(1))) {
            ic.beginBatchEdit();
            ic.deleteSurroundingText(2, 0);
            ic.commitText(lastTwo.charAt(1) + " ", 1);
            ic.endBatchEdit();
            mJustAddedAutoSpace = true;
            Log.d(TAG, "swapPunctuationAndSpace: YES");
        }
    }

    private void swapPeriodAndSpace() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
        if (lastThree != null && lastThree.length() == 3
                && lastThree.charAt(0) == '.'
                && lastThree.charAt(1) == KeyCodes.SPACE
                && lastThree.charAt(2) == '.') {
            ic.beginBatchEdit();
            ic.deleteSurroundingText(3, 0);
            ic.commitText(".. ", 1);
            ic.endBatchEdit();
        }
    }

    private boolean doubleSpace() {
        // if (!mAutoPunctuate) return;
        if (!mAskPrefs.isDoubleSpaceChangesToPeriod())
            return false;
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return false;
        CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
        if (lastThree != null && lastThree.length() == 3
                && Character.isLetterOrDigit(lastThree.charAt(0))
                && lastThree.charAt(1) == KeyCodes.SPACE
                && lastThree.charAt(2) == KeyCodes.SPACE) {
            ic.beginBatchEdit();
            ic.deleteSurroundingText(2, 0);
            ic.commitText(". ", 1);
            ic.endBatchEdit();
            mJustAddedAutoSpace = true;
            return true;
        }
        return false;
    }

    private void removeTrailingSpace() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;

        CharSequence lastOne = ic.getTextBeforeCursor(1, 0);
        if (lastOne != null && lastOne.length() == 1
                && lastOne.charAt(0) == KeyCodes.SPACE) {
            ic.deleteSurroundingText(1, 0);
        }
    }

    public boolean addWordToDictionary(String word) {
        if (mUserDictionary != null) {
            boolean added = mUserDictionary.addWord(word, 128);
            if (added && mCandidateView != null)
                mCandidateView.notifyAboutWordAdded(word);
            return added;
        } else {
            return false;
        }
    }

    public void removeFromUserDictionary(String word) {
        mJustAutoAddedWord = false;
        if (mUserDictionary != null) {
            mUserDictionary.deleteWord(word);
            abortCorrection(true, false);
            if (mCandidateView != null)
                mCandidateView.notifyAboutRemovedWord(word);
        }
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        // inner letters have more options: ' in English. " in Hebrew, and more.
        if (mPredicting)
            return getCurrentKeyboard().isInnerWordLetter((char) code);
        else
            return getCurrentKeyboard().isStartOfWordLetter((char) code);
    }

    public void onMultiTapStarted() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            ic.beginBatchEdit();
        handleDeleteLastCharacter(true);
        if (mInputView != null)
            mInputView.setShifted(mLastCharacterWasShifted);
    }

    public void onMultiTapEnded() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            ic.endBatchEdit();
    }

    public void onKey(int primaryCode, Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        Log.d(TAG, "onKey " + primaryCode);
        final InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case KeyCodes.ENTER:
            case KeyCodes.SPACE:
                //shortcut. Nothing more.
                handleSeparator(primaryCode);
                //should we switch to alphabet keyboard?
                if (!mKeyboardSwitcher.isAlphabetMode()) {
                    Log.d(TAG, "SPACE/ENTER while in symbols mode");
                    if (mAskPrefs.getSwitchKeyboardOnSpace()) {
                        Log.d(TAG, "Switching to Alphabet is required by the user");
                        mKeyboardSwitcher.nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
                    }
                }
                break;
            case KeyCodes.DELETE_WORD:
                if (ic == null)// if we don't want to do anything, lets check
                    // null first.
                    break;
                handleBackWord(ic);
                break;
            case KeyCodes.DELETE:
                if (ic == null)// if we don't want to do anything, lets check null first.
                    break;
                // we do backword if the shift is pressed while pressing
                // backspace (like in a PC)
                // but this is true ONLY if the device has multitouch, or the
                // user specifically asked for it
                if (mInputView != null
                        && mInputView.isShifted()
                        && !mInputView.getKeyboard().isShiftLocked()
                        && ((mDistinctMultiTouch && mShiftKeyState.isPressed()) || mAskPrefs.useBackword())) {
                    handleBackWord(ic);
                } else {
                    handleDeleteLastCharacter(false);
                }
                break;
            case KeyCodes.CLEAR_INPUT:
                if (ic != null) {
                    ic.beginBatchEdit();
                    commitTyped(ic);
                    ic.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    ic.endBatchEdit();
                }
                break;
            case KeyCodes.CTRL:
                if (fromUI) {
                    handleControl();
                } else {
                    //not from UI (user not actually pressed that button)
                    onPress(primaryCode);
                    onRelease(primaryCode);
                }
                break;
            case KeyCodes.SHIFT:
                if (fromUI) {
                    handleShift();
                } else {
                    //not from UI (user not actually pressed that button)
                    onPress(primaryCode);
                    onRelease(primaryCode);
                }
                break;
            case KeyCodes.ARROW_LEFT:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case KeyCodes.ARROW_RIGHT:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case KeyCodes.ARROW_UP:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case KeyCodes.ARROW_DOWN:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case KeyCodes.MOVE_HOME:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(0x0000007a/*API 11:KeyEvent.KEYCODE_MOVE_HOME*/);
                } else {
                    if (ic != null) {
                        CharSequence textBefore = ic.getTextBeforeCursor(1024, 0);
                        if (!TextUtils.isEmpty(textBefore)) {
                            int newPosition = textBefore.length() - 1;
                            while (newPosition > 0) {
                                char chatAt = textBefore.charAt(newPosition - 1);
                                if (chatAt == '\n' || chatAt == '\r') {
                                    break;
                                }
                                newPosition--;
                            }
                            if (newPosition < 0)
                                newPosition = 0;
                            ic.setSelection(newPosition, newPosition);
                        }
                    }
                }
                break;
            case KeyCodes.MOVE_END:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    //API 11: KeyEvent.KEYCODE_MOVE_END
                    sendDownUpKeyEvents(0x0000007b);
                } else {
                    if (ic != null) {
                        CharSequence textAfter = ic.getTextAfterCursor(1024, 0);
                        if (!TextUtils.isEmpty(textAfter)) {
                            int newPosition = 1;
                            while (newPosition < textAfter.length()) {
                                char chatAt = textAfter.charAt(newPosition);
                                if (chatAt == '\n' || chatAt == '\r') {
                                    break;
                                }
                                newPosition++;
                            }
                            if (newPosition > textAfter.length())
                                newPosition = textAfter.length();
                            try {
                                CharSequence textBefore = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
                                if (!TextUtils.isEmpty(textBefore)) {
                                    newPosition = newPosition + textBefore.length();
                                }
                                ic.setSelection(newPosition, newPosition);
                            } catch (Throwable e/*I'm using Integer.MAX_VALUE, it's scary.*/) {
                                Log.w(TAG, "Failed to getTextBeforeCursor.", e);
                            }
                        }
                    }
                }
                break;
            case KeyCodes.VOICE_INPUT:
                if (mVoiceRecognitionTrigger.isInstalled()) {
                    mVoiceRecognitionTrigger.startVoiceRecognition(getCurrentKeyboard().getDefaultDictionaryLocale());
                } else {
                    Intent voiceInputNotInstalledIntent = new Intent(getApplicationContext(), VoiceInputNotInstalledActivity.class);
                    voiceInputNotInstalledIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(voiceInputNotInstalledIntent);
                }
                break;
            case KeyCodes.CANCEL:
                if (mOptionsDialog == null || !mOptionsDialog.isShowing()) {
                    handleClose();
                }
                break;
            case KeyCodes.SETTINGS:
                showOptionsMenu();
                break;
            case KeyCodes.SPLIT_LAYOUT:
            case KeyCodes.MERGE_LAYOUT:
            case KeyCodes.COMPACT_LAYOUT_TO_RIGHT:
            case KeyCodes.COMPACT_LAYOUT_TO_LEFT:
                if (getCurrentKeyboard() != null && mInputView != null) {
                    mKeyboardInCondensedMode = CondenseType.fromKeyCode(primaryCode);
                    AnyKeyboard currentKeyboard = getCurrentKeyboard();
                    setKeyboardStuffBeforeSetToView(currentKeyboard);
                    mInputView.setKeyboard(currentKeyboard);
                }
                break;
            case KeyCodes.DOMAIN:
                onText(key, mAskPrefs.getDomainText());
                break;
            case KeyCodes.QUICK_TEXT:
                if (mDoNotFlipQuickTextKeyAndPopupFunctionality) {
                    outputCurrentQuickTextKey(key);
                } else {
                    openQuickTextPopup(key);
                }
                break;
            case KeyCodes.QUICK_TEXT_POPUP:
                if (mDoNotFlipQuickTextKeyAndPopupFunctionality) {
                    openQuickTextPopup(key);
                } else {
                    outputCurrentQuickTextKey(key);
                }
                break;
            case KeyCodes.MODE_SYMOBLS:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
                break;
            case KeyCodes.MODE_ALPHABET:
                if (mKeyboardSwitcher.shouldPopupForLanguageSwitch()) {
                    showLanguageSelectionDialog();
                } else
                    nextKeyboard(getCurrentInputEditorInfo(),
                            NextKeyboardType.Alphabet);
                break;
            case KeyCodes.UTILITY_KEYBOARD:
                mInputView.openUtilityKeyboard();
                break;
            case KeyCodes.MODE_ALPHABET_POPUP:
                showLanguageSelectionDialog();
                break;
            case KeyCodes.ALT:
                nextAlterKeyboard(getCurrentInputEditorInfo());
                break;
            case KeyCodes.KEYBOARD_CYCLE:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Any);
                break;
            case KeyCodes.KEYBOARD_REVERSE_CYCLE:
                nextKeyboard(getCurrentInputEditorInfo(),
                        NextKeyboardType.PreviousAny);
                break;
            case KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE:
                nextKeyboard(getCurrentInputEditorInfo(),
                        NextKeyboardType.AnyInsideMode);
                break;
            case KeyCodes.KEYBOARD_MODE_CHANGE:
                nextKeyboard(getCurrentInputEditorInfo(),
                        NextKeyboardType.OtherMode);
                break;
            case KeyCodes.CLIPBOARD:
                Clipboard cp = AnyApplication.getFrankenRobot().embody(
                        new Clipboard.ClipboardDiagram(getApplicationContext()));
                CharSequence clipboardText = cp.getText();
                if (!TextUtils.isEmpty(clipboardText)) {
                    onText(key, clipboardText);
                }
                break;
            case KeyCodes.TAB:
                sendTab();
                break;
            case KeyCodes.ESCAPE:
                sendEscape();
                break;
            default:
                // Issue 146: Right to left langs require reversed parenthesis
                if (mKeyboardSwitcher.isRightToLeftMode()) {
                    if (primaryCode == (int) ')')
                        primaryCode = (int) '(';
                    else if (primaryCode == (int) '(')
                        primaryCode = (int) ')';
                }

                if (isWordSeparator(primaryCode)) {
                    handleSeparator(primaryCode);
                } else {
                    if (mControlKeyState.isActive() && primaryCode >= 32 && primaryCode < 127) {
                        // http://en.wikipedia.org/wiki/Control_character#How_control_characters_map_to_keyboards
                        int controlCode = primaryCode & 31;
                        Log.d(TAG, "CONTROL state: Char was %d and now it is %d", primaryCode, controlCode);
                        if (controlCode == 9) {
                            sendTab();
                        } else {
                            ic.commitText(Character.toString((char) controlCode), 1);
                        }
                    } else {
                        handleCharacter(primaryCode, key, multiTapIndex,
                                nearByKeyCodes);
                    }
                    // resetting the mSpaceSent, which is set to true upon selecting candidate
                    mJustAddedAutoSpace = false;
                }
                break;
        }
    }

    private void openQuickTextPopup(Key key) {
        if (mInputView != null) {
            mInputView.showQuickKeysView(key);
        }
    }

    private void outputCurrentQuickTextKey(Key key) {
        QuickTextKey quickTextKey = QuickTextKeyFactory.getCurrentQuickTextKey(this);
        if (TextUtils.isEmpty(mOverrideQuickTextText))
            onText(key, quickTextKey.getKeyOutputText());
        else
            onText(key, mOverrideQuickTextText);
    }

    private boolean isTerminalEmulation() {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) return false;

        switch(ei.packageName) {
            case "org.connectbot":
            case "org.woltage.irssiconnectbot":
            case "com.pslib.connectbot":
            case "com.sonelli.juicessh":
                return ei.inputType == 0;
            default:
                return false;
        }
    }

    private void sendTab() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        boolean tabHack = isTerminalEmulation();

        // Note: tab and ^I don't work in ConnectBot, hackish workaround
        if (tabHack) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_DPAD_CENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_DPAD_CENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_I));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_I));
        } else {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_TAB));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_TAB));
        }
    }

    private void sendEscape() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (isTerminalEmulation()) {
            sendKeyChar((char) 27);
        } else {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, 111 /* KEYCODE_ESCAPE */));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, 111 /* KEYCODE_ESCAPE */));
        }
    }

    public void setKeyboardStuffBeforeSetToView(AnyKeyboard currentKeyboard) {
        currentKeyboard.setCondensedKeys(mKeyboardInCondensedMode);
    }

    private void showLanguageSelectionDialog() {
        KeyboardAddOnAndBuilder[] builders = mKeyboardSwitcher
                .getEnabledKeyboardsBuilders();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(getResources().getString(
                R.string.select_keyboard_popup_title));
        builder.setNegativeButton(android.R.string.cancel, null);
        ArrayList<CharSequence> keyboardsIds = new ArrayList<>();
        ArrayList<CharSequence> keyboards = new ArrayList<>();
        // going over all enabled keyboards
        for (KeyboardAddOnAndBuilder keyboardBuilder : builders) {
            keyboardsIds.add(keyboardBuilder.getId());
            String name = keyboardBuilder.getName();

            keyboards.add(name);
        }

        final CharSequence[] ids = new CharSequence[keyboardsIds.size()];
        final CharSequence[] items = new CharSequence[keyboards.size()];
        keyboardsIds.toArray(ids);
        keyboards.toArray(items);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int position) {
                di.dismiss();

                if ((position < 0) || (position >= items.length)) {
                    Log.d(TAG, "Keyboard selection popup canceled");
                } else {
                    CharSequence id = ids[position];
                    Log.d(TAG, "User selected '%s' with id %s", items[position], id);
                    EditorInfo currentEditorInfo = getCurrentInputEditorInfo();
                    mKeyboardSwitcher.nextAlphabetKeyboard(currentEditorInfo, id.toString());
                    setKeyboardFinalStuff(NextKeyboardType.Alphabet);
                }
            }
        });

        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    public void onText(Key key, CharSequence text) {
        Log.d(TAG, "onText: '%s'", text);
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        ic.beginBatchEdit();
        if (mPredicting) {
            commitTyped(ic);
        }
        abortCorrection(true, false);
        ic.commitText(text, 1);
        ic.endBatchEdit();

        mJustAddedAutoSpace = false;
        mJustAddOnText = text;
        mCommittedWord = text;

        setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, false), false, false, false);
    }

    private boolean performOnTextDeletion(InputConnection ic) {
        if (mJustAddOnText != null && ic != null) {
            final CharSequence onTextText = mJustAddOnText;
            mJustAddOnText = null;
            //just now, the user had cause onText to add text to input.
            //but after that, immediately pressed delete. So I'm guessing deleting the entire text is needed
            final int onTextLength = onTextText.length();
            Log.d(TAG, "Deleting the entire 'onText' input " + onTextText);
            CharSequence cs = ic.getTextBeforeCursor(onTextLength, 0);
            if (onTextText.equals(cs)) {
                ic.deleteSurroundingText(onTextLength, 0);
                return true;
            }
        }

        return false;
    }

    private void handleBackWord(InputConnection ic) {
        if (ic == null) {
            return;
        }

        if (performOnTextDeletion(ic))
            return;

        if (mPredicting) {
            mWord.reset();
            mSuggest.resetNextWordSentence();
            mPredicting = false;
            ic.setComposingText("", 1);
            postUpdateSuggestions();
            return;
        }
        // I will not delete more than 128 characters. Just a safe-guard.
        // this will also allow me do just one call to getTextBeforeCursor!
        // Which is always good. This is a part of issue 951.
        CharSequence cs = ic.getTextBeforeCursor(128, 0);
        if (TextUtils.isEmpty(cs)) {
            return;// nothing to delete
        }
        // TWO OPTIONS
        // 1) Either we do like Linux and Windows (and probably ALL desktop
        // OSes):
        // Delete all the characters till a complete word was deleted:
        /*
         * What to do: We delete until we find a separator (the function
         * isBackWordStopChar). Note that we MUST delete a delete a whole word!
         * So if the back-word starts at separators, we'll delete those, and then
         * the word before: "test this,       ," -> "test "
         */
        // Pro: same as desktop
        // Con: when auto-caps is on (the default), this will delete the
        // previous word, which can be annoying..
        // E.g., Writing a sentence, then a period, then ASK will auto-caps,
        // then when the user press backspace (for some reason),
        // the entire previous word deletes.

        // 2) Or we delete all the characters till we encounter a separator, but
        // delete at least one character.
        /*
         * What to do: We delete until we find a separator (the function
         * isBackWordStopChar). Note that we MUST delete a delete at least one
         * character "test this, " -> "test this," -> "test this" -> "test "
         */
        // Pro: Supports auto-caps, and mostly similar to desktop OSes
        // Con: Not all desktop use-cases are here.

        // For now, I go with option 2, but I'm open for discussion.

        // 2b) "test this, " -> "test this"

        final int inputLength = cs.length();
        int idx = inputLength - 1;// it's OK since we checked whether cs is
        // empty after retrieving it.
        while (idx > 0 && !isBackWordStopChar((int) cs.charAt(idx))) {
            idx--;
        }
        ic.deleteSurroundingText(inputLength - idx, 0);// it is always > 0 !
    }

    private void handleDeleteLastCharacter(boolean forMultiTap) {
        InputConnection ic = getCurrentInputConnection();

        if (!forMultiTap && performOnTextDeletion(ic))
            return;

        boolean deleteChar = false;
        if (mPredicting) {
            final boolean wordManipulation = mWord.length() > 0
                    && mWord.cursorPosition() > 0;
            if (wordManipulation) {
                mWord.deleteLast();
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length())
                    cursorPosition = getCursorPosition(ic);
                else
                    cursorPosition = -1;

                if (cursorPosition >= 0)
                    ic.beginBatchEdit();

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (mWord.length() == 0) {
                    mPredicting = false;
                } else if (cursorPosition >= 0) {
                    ic.setSelection(cursorPosition - 1, cursorPosition - 1);
                }

                if (cursorPosition >= 0)
                    ic.endBatchEdit();

                postUpdateSuggestions();
            } else {
                ic.deleteSurroundingText(1, 0);
            }
        } else {
            deleteChar = true;
        }

        TextEntryState.backspace();
        if (TextEntryState.getState() == TextEntryState.State.UNDO_COMMIT) {
            revertLastWord(deleteChar);
        } else if (deleteChar) {
            if (mCandidateView != null
                    && mCandidateView.dismissAddToDictionaryHint()) {
                // Go back to the suggestion mode if the user canceled the
                // "Touch again to save".
                // NOTE: we don't revert the word when backspacing
                // from a manual suggestion pick. We deliberately chose a
                // different behavior only in the case of picking the first
                // suggestion (typed word). It's intentional to have made this
                // inconsistent with backspacing after selecting other
                // suggestions.
                revertLastWord(true/*this is a Delete character*/);
            } else {
                if (!forMultiTap) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                } else {
                    // this code tries to delete the text in a different way,
                    // because of multi-tap stuff
                    // using "deleteSurroundingText" will actually get the input
                    // updated faster!
                    // but will not handle "delete all selected text" feature,
                    // hence the "if (!forMultiTap)" above
                    final CharSequence beforeText = ic == null ? null : ic.getTextBeforeCursor(1, 0);
                    final int textLengthBeforeDelete = (TextUtils.isEmpty(beforeText)) ? 0 : beforeText.length();
                    if (textLengthBeforeDelete > 0)
                        ic.deleteSurroundingText(1, 0);
                    else
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                }
            }
        }
    }

    private void handleControl() {
        if (mInputView != null && mKeyboardSwitcher.isAlphabetMode()) {
            mInputView.setControl(mControlKeyState.isActive());
        }
    }

    private void handleShift() {
        if (mInputView != null) {
            Log.d(TAG, "shift Setting UI active:%s, locked: %s", mShiftKeyState.isActive(), mShiftKeyState.isLocked());
            mInputView.setShifted(mShiftKeyState.isActive());
            mInputView.setShiftLocked(mShiftKeyState.isLocked());
        }
    }

    private void abortCorrection(boolean force, boolean forever) {
        mSuggest.resetNextWordSentence();
        mJustAutoAddedWord = false;
        if (force || TextEntryState.isCorrecting()) {
            Log.d(TAG, "abortCorrection will actually abort correct");
            mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
            mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);

            final InputConnection ic = getCurrentInputConnection();
            if (ic != null)
                ic.finishComposingText();

            clearSuggestions();

            TextEntryState.reset();
            mUndoCommitCursorPosition = -2;
            mWord.reset();
            mPredicting = false;
            mJustAddedAutoSpace = false;
            mJustAutoAddedWord = false;
            if (forever) {
                Log.d(TAG, "abortCorrection will abort correct forever");
                mPredictionOn = false;
                setCandidatesViewShown(false);
                if (mSuggest != null) {
                    mSuggest.setCorrectionMode(false, false, 0, 0);
                }
            }
        }
    }

    private void handleCharacter(final int primaryCode, Key key,
                                 int multiTapIndex, int[] nearByKeyCodes) {
        Log.d(TAG, "handleCharacter: " + primaryCode + ", isPredictionOn:"
                + isPredictionOn() + ", mPredicting:" + mPredicting);
        if (!mPredicting && isPredictionOn() && isAlphabet(primaryCode)
                && !isCursorTouchingWord()) {
            mPredicting = true;
            mUndoCommitCursorPosition = -2;// so it will be marked the next time
            mWord.reset();
            mAutoCorrectOn = mAutoComplete;
        }

        mLastCharacterWasShifted = (mInputView != null) && mInputView.isShifted();

        final int primaryCodeToOutput;
        if (mShiftKeyState.isActive()) {
            if (key != null) {
                primaryCodeToOutput = key.getCodeAtIndex(multiTapIndex, true);
            } else {
                primaryCodeToOutput = Character.toUpperCase(primaryCode);
            }
        } else {
            primaryCodeToOutput = primaryCode;
        }

        if (mPredicting) {
            if (mShiftKeyState.isActive() && mWord.cursorPosition() == 0) {
                mWord.setFirstCharCapitalized(true);
            }

            final InputConnection ic = getCurrentInputConnection();
            if (mWord.add(primaryCodeToOutput, nearByKeyCodes)) {
                Toast note = Toast
                        .makeText(
                                getApplicationContext(),
                                "Check the logcat for a note from AnySoftKeyboard developers!",
                                Toast.LENGTH_LONG);
                note.show();

                Log.i(TAG,
                        "*******************"
                                + "\nNICE!!! You found the our easter egg! http://www.dailymotion.com/video/x3zg90_gnarls-barkley-crazy-2006-mtv-star_music\n"
                                + "\nAnySoftKeyboard R&D team would like to thank you for using our keyboard application."
                                + "\nWe hope you enjoying it, we enjoyed making it."
                                + "\nWhile developing this application, we heard Gnarls Barkley's Crazy quite a lot, and would like to share it with you."
                                + "\n"
                                + "\nThanks."
                                + "\nMenny Even Danan, Hezi Cohen, Hugo Lopes, Henrik Andersson, Sami Salonen, and Lado Kumsiashvili."
                                + "\n*******************");

                Intent easterEgg = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.dailymotion.com/video/x3zg90_gnarls-barkley-crazy-2006-mtv-star_music"));
                easterEgg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(easterEgg);
            }
            if (ic != null) {
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length()) {
                    Log.d(TAG,
                            "Cursor is not at the end of the word. I'll need to reposition");
                    cursorPosition = getCursorPosition(ic);
                } else {
                    cursorPosition = -1;
                }

                if (cursorPosition >= 0)
                    ic.beginBatchEdit();

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (cursorPosition >= 0) {
                    ic.setSelection(cursorPosition + 1, cursorPosition + 1);
                    ic.endBatchEdit();
                }
            }
            // this should be done ONLY if the key is a letter, and not a inner
            // character (like ').
            if (Character.isLetter((char) primaryCodeToOutput)) {
                postUpdateSuggestions();
            } else {
                // just replace the typed word in the candidates view
                if (mCandidateView != null)
                    mCandidateView.replaceTypedWord(mWord.getTypedWord());
            }
        } else {
            sendKeyChar((char) primaryCodeToOutput);
        }
        TextEntryState.typedCharacter((char) primaryCodeToOutput, false);
        mJustAutoAddedWord = false;
    }

    private void handleSeparator(int primaryCode) {
        Log.d(TAG, "handleSeparator: " + primaryCode);

        //will not show next-word suggestion in case of a new line or if the separator is a sentence separator.
        boolean isEndOfSentence = (primaryCode == KeyCodes.ENTER || mSentenceSeparators.contains(Character.valueOf((char)primaryCode)));

        // Should dismiss the "Touch again to save" message when handling
        // separator
        if (mCandidateView != null && mCandidateView.dismissAddToDictionaryHint()) {
            postUpdateSuggestions();
        }

        boolean pickedDefault = false;
        // Handle separator
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        // this is a special case, when the user presses a separator WHILE
        // inside the predicted word.
        // in this case, I will want to just dump the separator.
        final boolean separatorInsideWord = (mWord.cursorPosition() < mWord.length());
        if (mPredicting && !separatorInsideWord) {
            // In certain languages where single quote is a separator, it's
            // better
            // not to auto correct, but accept the typed word. For instance,
            // in Italian dov' should not be expanded to dove' because the
            // elision
            // requires the last vowel to be removed.
            //Also, ACTION does not invoke default picking. See https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/198
            if (mAutoCorrectOn && primaryCode != '\'' && primaryCode != KeyCodes.ENTER) {
                pickedDefault = pickDefaultSuggestion();
                // Picked the suggestion by the space key. We consider this
                // as "added an auto space".
                if (primaryCode == KeyCodes.SPACE) {
                    mJustAddedAutoSpace = true;
                }
            } else {
                commitTyped(ic);
                if (isEndOfSentence) abortCorrection(true, false);
            }
        } else if (separatorInsideWord) {
            // when putting a separator in the middle of a word, there is no
            // need to do correction, or keep knowledge
            abortCorrection(true, false);
        }

        if (mJustAddedAutoSpace && primaryCode == KeyCodes.ENTER) {
            removeTrailingSpace();
            mJustAddedAutoSpace = false;
        }

        final EditorInfo ei = getCurrentInputEditorInfo();
        if (primaryCode == KeyCodes.ENTER && mShiftKeyState.isActive() && ic != null && ei != null && (ei.imeOptions & EditorInfo.IME_MASK_ACTION) != EditorInfo.IME_ACTION_NONE) {
            //power-users feature ahead: Shift+Enter
            //getting away from firing the default editor action, by forcing newline
            ic.commitText("\n", 1);
        } else {
            sendKeyChar((char) primaryCode);
        }

        // Handle the case of ". ." -> " .." with auto-space if necessary
        // before changing the TextEntryState.
        if (mJustAddedAutoSpace && primaryCode == '.') {
            swapPeriodAndSpace();
        }

        TextEntryState.typedCharacter((char) primaryCode, true);

        if (TextEntryState.getState() == TextEntryState.State.PUNCTUATION_AFTER_ACCEPTED
                && primaryCode != KeyCodes.ENTER) {
            swapPunctuationAndSpace();
        } else if (/* isPredictionOn() && */primaryCode == ' ') {
            if (doubleSpace()) {
                isEndOfSentence = true;
            }
        }
        if (pickedDefault && mWord.getPreferredWord() != null) {
            TextEntryState.acceptedDefault(mWord.getTypedWord(), mWord.getPreferredWord());
        }
        if (ic != null) {
            ic.endBatchEdit();
        }

        if (isEndOfSentence) {
            mSuggest.resetNextWordSentence();
            clearSuggestions();
        } else if (!TextUtils.isEmpty(mCommittedWord)) {
            setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, mWord.isAllUpperCase()), false, false, false);
            mWord.setFirstCharCapitalized(false);
        }
    }

    private void handleClose() {
        boolean closeSelf = true;

        if (mInputView != null)
            closeSelf = mInputView.closing();

        if (closeSelf) {
            commitTyped(getCurrentInputConnection());
            requestHideSelf(0);
            abortCorrection(true, true);
            TextEntryState.endSession();
        }
    }

    private void postUpdateSuggestions() {
        postUpdateSuggestions(5 * ONE_FRAME_DELAY);
    }

    /**
     * posts an update suggestions request to the messages queue. Removes any previous request.
     *
     * @param delay negative value will cause the call to be done now, in this thread.
     */
    private void postUpdateSuggestions(long delay) {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        if (delay > 0)
            mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS), delay);
        else if (delay == 0)
            mKeyboardHandler.sendMessage(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS));
        else
            performUpdateSuggestions();
    }

    private boolean isPredictionOn() {
        return mPredictionOn;
    }

    private boolean shouldCandidatesStripBeShown() {
        return mShowSuggestions && onEvaluateInputViewShown();
    }

    /*package*/ void performUpdateSuggestions() {
        Log.d(TAG, "performUpdateSuggestions: has mSuggest:"
                + (mSuggest != null) + ", isPredictionOn:"
                + isPredictionOn() + ", mPredicting:" + mPredicting
                + ", mQuickFixes:" + mQuickFixes + " mShowSuggestions:"
                + mShowSuggestions);
        // Check if we have a suggestion engine attached.
        if (mSuggest == null) {
            return;
        }

        // final boolean showSuggestions = (mCandidateView != null &&
        // mPredicting
        // && isPredictionOn() && shouldCandidatesStripBeShown());

        if (mCandidateCloseText != null)// in API3 this variable is null
            mCandidateCloseText.setVisibility(View.GONE);

        if (!mPredicting) {
            clearSuggestions();
            return;
        }

        List<CharSequence> stringList = mSuggest.getSuggestions(/* mInputView, */mWord, false);
        boolean correctionAvailable = mSuggest.hasMinimalCorrection();
        // || mCorrectionMode == mSuggest.CORRECTION_FULL;
        CharSequence typedWord = mWord.getTypedWord();
        // If we're in basic correct
        boolean typedWordValid = mSuggest.isValidWord(typedWord);

        if (mShowSuggestions || mQuickFixes) {
            correctionAvailable |= typedWordValid;
        }

        // Don't auto-correct words with multiple capital letter
        correctionAvailable &= !mWord.isMostlyCaps();
        correctionAvailable &= !TextEntryState.isCorrecting();

        setSuggestions(stringList, false, typedWordValid, correctionAvailable);
        if (stringList.size() > 0) {
            if (correctionAvailable && !typedWordValid && stringList.size() > 1) {
                mWord.setPreferredWord(stringList.get(1));
            } else {
                mWord.setPreferredWord(typedWord);
            }
        } else {
            mWord.setPreferredWord(null);
        }
        setCandidatesViewShown(shouldCandidatesStripBeShown() || mCompletionOn);
    }

    private boolean pickDefaultSuggestion() {

        // Complete any pending candidate query first
        if (mKeyboardHandler.hasMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS)) {
            postUpdateSuggestions(-1);
        }

        final CharSequence bestWord = mWord.getPreferredWord();
        Log.d(TAG, "pickDefaultSuggestion: bestWord:" + bestWord);

        if (!TextUtils.isEmpty(bestWord)) {
            final CharSequence typedWord = mWord.getTypedWord();
            TextEntryState.acceptedDefault(typedWord, bestWord);
            final boolean fixed = !typedWord.equals(pickSuggestion(bestWord, !bestWord.equals(typedWord)));
            if (!fixed) {//if the word typed was auto-replaced, we should not learn it.
                // Add the word to the auto dictionary if it's not a known word
                addToDictionaries(mWord, AutoDictionary.AdditionType.Typed);
            }
            return true;
        }
        return false;
    }

    public void pickSuggestionManually(int index, CharSequence suggestion) {
        final boolean correcting = TextEntryState.isCorrecting();
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        try {
            if (mCompletionOn && mCompletions != null && index >= 0 && index < mCompletions.length) {
                CompletionInfo ci = mCompletions[index];
                if (ic != null) {
                    ic.commitCompletion(ci);
                }
                mCommittedLength = suggestion.length();
                mCommittedWord = suggestion;
                if (mCandidateView != null) {
                    mCandidateView.clear();
                }
                return;
            }
            pickSuggestion(suggestion, correcting);

            TextEntryState.acceptedSuggestion(mWord.getTypedWord(), suggestion);
            // Follow it with a space
            if (mAutoSpace && !correcting) {
                sendSpace();
                mJustAddedAutoSpace = true;
            }
            // Add the word to the auto dictionary if it's not a known word
            mJustAutoAddedWord = false;
            if (index == 0) {
                mJustAutoAddedWord = addToDictionaries(mWord, AutoDictionary.AdditionType.Picked);
            }

            final boolean showingAddToDictionaryHint = !mJustAutoAddedWord
                    && index == 0
                    && (mQuickFixes || mShowSuggestions)
                    && !mSuggest.isValidWord(suggestion)// this is for the case that the word was auto-added upon picking
                    && !mSuggest.isValidWord(suggestion.toString().toLowerCase(getCurrentKeyboard().getLocale()));

            if (!mJustAutoAddedWord) {
                if (showingAddToDictionaryHint && mCandidateView != null) {
                    mCandidateView.showAddToDictionaryHint(suggestion);
                }
            }
            if (!TextUtils.isEmpty(mCommittedWord)) {
                setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, mWord.isAllUpperCase()), false, false, false);
                mWord.setFirstCharCapitalized(false);
            }
        } finally {
            if (ic != null) {
                ic.endBatchEdit();
            }
        }
    }

    /**
     * Commits the chosen word to the text field and saves it for later
     * retrieval.
     *
     * @param suggestion the suggestion picked by the user to be committed to the text
     *                   field
     * @param correcting whether this is due to a correction of an existing word.
     */
    private CharSequence pickSuggestion(CharSequence suggestion, boolean correcting) {
        if (mShiftKeyState.isLocked()) {
            suggestion = suggestion.toString().toUpperCase(getCurrentKeyboard().getLocale());
        } else if (preferCapitalization() || (mKeyboardSwitcher.isAlphabetMode() && mShiftKeyState.isActive())) {
            suggestion = Character.toUpperCase(suggestion.charAt(0)) + suggestion.subSequence(1, suggestion.length()).toString();
        }

        mWord.setPreferredWord(suggestion);
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (correcting) {
                AnyApplication.getDeviceSpecific().commitCorrectionToInputConnection(ic, mWord);
                // and drawing pop-out text
                mInputView.popTextOutOfKey(mWord.getPreferredWord());
            } else {
                ic.commitText(suggestion, 1);
            }
        }
        mPredicting = false;
        mCommittedLength = suggestion.length();
        mCommittedWord = suggestion;

        clearSuggestions();

        return suggestion;
    }

    private boolean isCursorTouchingWord() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return false;

        CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        // It is not exactly clear to me why, but sometimes, although I request
        // 1 character, I get
        // the entire text. This causes me to incorrectly detect restart
        // suggestions...
        if (!TextUtils.isEmpty(toLeft) && toLeft.length() == 1
                && !isWordSeparator(toLeft.charAt(0))) {
            return true;
        }

        CharSequence toRight = ic.getTextAfterCursor(1, 0);
        return (!TextUtils.isEmpty(toRight)) &&
                (toRight.length() == 1) &&
                (!isWordSeparator(toRight.charAt(0)));
    }

    public void revertLastWord(boolean deleteChar) {
        Log.d(TAG, "revertLastWord deleteChar:" + deleteChar
                + ", mWord.size:" + mWord.length() + " mPredicting:"
                + mPredicting + " mCommittedLength" + mCommittedLength);

        final int length = mWord.length();// mComposing.length();
        if (!mPredicting && length > 0) {
            mAutoCorrectOn = false;
            final CharSequence typedWord = mWord.getTypedWord();
            final InputConnection ic = getCurrentInputConnection();
            mPredicting = true;
            mUndoCommitCursorPosition = -2;
            ic.beginBatchEdit();
            // mJustRevertedSeparator = ic.getTextBeforeCursor(1, 0);
            if (deleteChar)
                ic.deleteSurroundingText(1, 0);
            int toDelete = mCommittedLength;
            CharSequence toTheLeft = ic
                    .getTextBeforeCursor(mCommittedLength, 0);
            if (toTheLeft != null && toTheLeft.length() > 0
                    && isWordSeparator(toTheLeft.charAt(0))) {
                toDelete--;
            }
            ic.deleteSurroundingText(toDelete, 0);
            ic.setComposingText(typedWord/* mComposing */, 1);
            TextEntryState.backspace();
            ic.endBatchEdit();
            postUpdateSuggestions(-1);
            if (mJustAutoAddedWord && mUserDictionary != null) {
                // we'll also need to REMOVE the word from the user dictionary
                // now...
                // Since the user revert the committed word, and ASK auto-added
                // that word, this word will need to be removed.
                removeFromUserDictionary(typedWord.toString());
            }
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            // mJustRevertedSeparator = null;
        }
    }

    public boolean isWordSeparator(int code) {
        return (!isAlphabet(code));
    }

    private void sendSpace() {
        sendKeyChar((char) KeyCodes.SPACE);
    }

    public boolean preferCapitalization() {
        return mWord.isFirstCharCapitalized();
    }

    private void nextAlterKeyboard(EditorInfo currentEditorInfo) {
        Log.d(TAG, "nextAlterKeyboard: currentEditorInfo.inputType="
                + currentEditorInfo.inputType);

        // AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
        if (getCurrentKeyboard() == null) {
            Log.d(TAG,
                    "nextKeyboard: Looking for next keyboard. No current keyboard.");
        } else {
            Log.d(TAG,
                    "nextKeyboard: Looking for next keyboard. Current keyboard is:"
                            + getCurrentKeyboard().getKeyboardName());
        }

        mKeyboardSwitcher.nextAlterKeyboard(currentEditorInfo);

        Log.i(TAG, "nextAlterKeyboard: Setting next keyboard to: "
                + getCurrentKeyboard().getKeyboardName());
    }

    private void nextKeyboard(EditorInfo currentEditorInfo,
                              KeyboardSwitcher.NextKeyboardType type) {
        Log.d(TAG, "nextKeyboard: currentEditorInfo.inputType="
                + currentEditorInfo.inputType + " type:" + type);

        // in numeric keyboards, the LANG key will go back to the original
        // alphabet keyboard-
        // so no need to look for the next keyboard, 'mLastSelectedKeyboard'
        // holds the last
        // keyboard used.
        AnyKeyboard keyboard = mKeyboardSwitcher.nextKeyboard(currentEditorInfo, type);

        if (!(keyboard instanceof GenericKeyboard)) {
            mSentenceSeparators = keyboard.getSentenceSeparators();
        }
        setKeyboardFinalStuff(type);
    }

    private void setKeyboardFinalStuff(KeyboardSwitcher.NextKeyboardType type) {
        mShiftKeyState.reset();
        mControlKeyState.reset();
        mSuggest.resetNextWordSentence();
        // changing dictionary
        setDictionariesForCurrentKeyboard();
        // Notifying if needed
        if ((mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS))
                || (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ON_PHYSICAL) && (type == NextKeyboardType.AlphabetSupportsPhysical))) {
            notifyKeyboardChangeIfNeeded();
        }
        postUpdateSuggestions();
        updateShiftStateNow();
    }

    public void onSwipeRight(boolean onSpaceBar, boolean twoFingersGesture) {
        final int keyCode = mAskPrefs.getGestureSwipeRightKeyCode(onSpaceBar, twoFingersGesture);
        Log.d(TAG, "onSwipeRight " + ((onSpaceBar) ? " + space" : "") + ((twoFingersGesture) ? " + two-fingers" : "") + " => code " + keyCode);
        if (keyCode != 0) mSwitchAnimator.doSwitchAnimation(AnimationType.SwipeRight, keyCode);
    }

    public void onSwipeLeft(boolean onSpaceBar, boolean twoFingersGesture) {
        final int keyCode = mAskPrefs.getGestureSwipeLeftKeyCode(onSpaceBar, twoFingersGesture);
        Log.d(TAG, "onSwipeLeft " + ((onSpaceBar) ? " + space" : "") + ((twoFingersGesture) ? " + two-fingers" : "") + " => code " + keyCode);
        if (keyCode != 0) mSwitchAnimator.doSwitchAnimation(AnimationType.SwipeLeft, keyCode);
    }

    public void onSwipeDown(boolean onSpaceBar) {
        final int keyCode = mAskPrefs.getGestureSwipeDownKeyCode();
        Log.d(TAG, "onSwipeDown " + ((onSpaceBar) ? " + space" : "") + " => code " + keyCode);
        if (keyCode != 0) onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    public void onSwipeUp(boolean onSpaceBar) {
        final int keyCode = mAskPrefs.getGestureSwipeUpKeyCode(onSpaceBar);
        Log.d(TAG, "onSwipeUp " + ((onSpaceBar) ? " + space" : "") + " => code " + keyCode);
        if (keyCode != 0) onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    public void onPinch() {
        final int keyCode = mAskPrefs.getGesturePinchKeyCode();
        Log.d(TAG, "onPinch => code " + keyCode);
        if (keyCode != 0) onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    public void onSeparate() {
        final int keyCode = mAskPrefs.getGestureSeparateKeyCode();
        Log.d(TAG, "onSeparate => code " + keyCode);
        if (keyCode != 0) onKey(keyCode, null, -1, new int[]{keyCode}, false/*not directly pressed the UI key*/);
    }

    private void sendKeyDown(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
    }

    private void sendKeyUp(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
    }

    public void onPress(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        Log.d(TAG, "onPress:" + primaryCode);
        if (mVibrationDuration > 0 && primaryCode != 0) {
            mVibrator.vibrate(mVibrationDuration);
        }

        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onPress();
            handleShift();
        } else {
            mShiftKeyState.onOtherKeyPressed();
        }

        if (primaryCode == KeyCodes.CTRL) {
            mControlKeyState.onPress();
            handleControl();
            sendKeyDown(ic, 113); // KeyEvent.KEYCODE_CTRL_LEFT (API 11 and up)
        } else {
            mControlKeyState.onOtherKeyPressed();
        }

        if (mSoundOn && (!mSilentMode) && primaryCode != 0) {
            final int keyFX;
            switch (primaryCode) {
                case 13:
                case KeyCodes.ENTER:
                    keyFX = AudioManager.FX_KEYPRESS_RETURN;
                    break;
                case KeyCodes.DELETE:
                    keyFX = AudioManager.FX_KEYPRESS_DELETE;
                    break;
                case KeyCodes.SPACE:
                    keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
                    break;
                default:
                    keyFX = AudioManager.FX_KEY_CLICK;
            }
            final float fxVolume;
            // creating scoop to make sure volume and maxVolume
            // are not used
            {
                final int volume;
                final int maxVolume;
                if (mSoundVolume > 0) {
                    volume = mSoundVolume;
                    maxVolume = 100;
                    fxVolume = ((float) volume) / ((float) maxVolume);
                } else {
                    fxVolume = -1.0f;
                }

            }

            Log.d(TAG, "Sound on key-pressed. Sound ID:" + keyFX
                    + " with volume " + fxVolume);

            mAudioManager.playSoundEffect(keyFX, fxVolume);
        }
    }

    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        Log.d(TAG, "onRelease:" + primaryCode);
        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onRelease(mAskPrefs.getMultiTapTimeout());
            handleShift();
        } else {
            if (mShiftKeyState.onOtherKeyReleased()) {
                updateShiftStateNow();
            }
        }

        if (primaryCode == KeyCodes.CTRL) {
            sendKeyUp(ic, 113); // KeyEvent.KEYCODE_CTRL_LEFT
            mControlKeyState.onRelease(mAskPrefs.getMultiTapTimeout());
            handleControl();
        } else {
            mControlKeyState.onOtherKeyReleased();
        }
    }

    // update flags for silent mode
    public void updateRingerMode() {
        mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
    }

    private void loadSettings() {
        // Get the settings preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        mVibrationDuration = Integer.parseInt(sp.getString(getString(R.string.settings_key_vibrate_on_key_press_duration), getString(R.string.settings_default_vibrate_on_key_press_duration)));

        mSoundOn = sp.getBoolean(getString(R.string.settings_key_sound_on),
                getResources().getBoolean(R.bool.settings_default_sound_on));
        if (mSoundOn) {
            Log.i(TAG, "Loading sounds effects from AUDIO_SERVICE due to configuration change.");
            try {
                mAudioManager.loadSoundEffects();
            } catch (SecurityException e) {
                //for unknown reason loadSoundEffects may throw SecurityException (happened on a HuaweiG750-U10/4.2.2).
                Log.w(TAG, "SecurityException swallowed. ", e);
                mSoundOn = false;
            }
        }
        // checking the volume
        boolean customVolume = sp.getBoolean("use_custom_sound_volume", false);
        int newVolume;
        if (customVolume) {
            newVolume = sp.getInt("custom_sound_volume", 0) + 1;
            Log.i(TAG, "Custom volume checked: " + newVolume + " out of 100");
        } else {
            Log.i(TAG, "Custom volume un-checked.");
            newVolume = -1;
        }
        mSoundVolume = newVolume;

        // in order to support the old type of configuration
        mKeyboardChangeNotificationType = sp.getString(
                getString(R.string.settings_key_physical_keyboard_change_notification_type),
                getString(R.string.settings_default_physical_keyboard_change_notification_type));

        // now clearing the notification, and it will be re-shown if needed
        mInputMethodManager.hideStatusIcon(mImeToken);
        // should it be always on?
        if (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
            notifyKeyboardChangeIfNeeded();
        }

        mAutoCap = sp.getBoolean("auto_caps", true);

        mShowSuggestions = sp.getBoolean("candidates_on", true);

        setDictionariesForCurrentKeyboard();

        final String autoPickAggressiveness = sp.getString(
                getString(R.string.settings_key_auto_pick_suggestion_aggressiveness),
                getString(R.string.settings_default_auto_pick_suggestion_aggressiveness));

        switch (autoPickAggressiveness) {
            case "none":
                mCalculatedCommonalityMaxLengthDiff = 0;
                mCalculatedCommonalityMaxDistance = 0;
                mAutoComplete = false;
                break;
            case "minimal_aggressiveness":
                mCalculatedCommonalityMaxLengthDiff = 1;
                mCalculatedCommonalityMaxDistance = 1;
                mAutoComplete = true;
                break;
            case "high_aggressiveness":
                mCalculatedCommonalityMaxLengthDiff = 3;
                mCalculatedCommonalityMaxDistance = 4;
                mAutoComplete = true;
                break;
            case "extreme_aggressiveness":
                mCalculatedCommonalityMaxLengthDiff = 5;
                mCalculatedCommonalityMaxDistance = 5;
                mAutoComplete = true;
                break;
            default:
                mCalculatedCommonalityMaxLengthDiff = 2;
                mCalculatedCommonalityMaxDistance = 3;
                mAutoComplete = true;
        }
        mAutoCorrectOn = mAutoComplete = mAutoComplete && mShowSuggestions;

        mQuickFixes = sp.getBoolean("quick_fix", true);

        mAllowSuggestionsRestart = sp.getBoolean(
                getString(R.string.settings_key_allow_suggestions_restart),
                getResources().getBoolean(R.bool.settings_default_allow_suggestions_restart));

        mMinimumWordCorrectionLength = sp.getInt(getString(R.string.settings_key_min_length_for_word_correction__), 2);
        if (mSuggest != null) {
            mSuggest.setMinimumWordLengthForCorrection(mMinimumWordCorrectionLength);
            mSuggest.setCorrectionMode(mQuickFixes, mShowSuggestions, mCalculatedCommonalityMaxLengthDiff, mCalculatedCommonalityMaxDistance);
        }

        mDoNotFlipQuickTextKeyAndPopupFunctionality = sp.getBoolean(
                getString(R.string.settings_key_do_not_flip_quick_key_codes_functionality),
                getResources().getBoolean(R.bool.settings_default_do_not_flip_quick_keys_functionality));

        mOverrideQuickTextText = sp.getString(getString(R.string.settings_key_emoticon_default_text), null);

        setInitialCondensedState(getResources().getConfiguration());
    }

    private void setDictionariesForCurrentKeyboard() {
        if (mSuggest != null) {
            if (!mPredictionOn) {
                Log.d(TAG,
                        "No suggestion is required. I'll try to release memory from the dictionary.");
                // DictionaryFactory.getInstance().releaseAllDictionaries();
                mSuggest.setMainDictionary(getApplicationContext(), null);
                mSuggest.setUserDictionary(null);
                mSuggest.setAutoDictionary(null);
                mLastDictionaryRefresh = -1;
            } else {
                mLastDictionaryRefresh = SystemClock.elapsedRealtime();
                // It null at the creation of the application.
                if ((mKeyboardSwitcher != null)
                        && mKeyboardSwitcher.isAlphabetMode()) {
                    AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();

                    // if there is a mapping in the settings, we'll use that,
                    // else we'll
                    // return the default
                    String mappingSettingsKey = getDictionaryOverrideKey(currentKeyboard);
                    String defaultDictionary = currentKeyboard.getDefaultDictionaryLocale();
                    String dictionaryValue = mPrefs.getString(mappingSettingsKey, null);

                    final DictionaryAddOnAndBuilder dictionaryBuilder;

                    if (dictionaryValue == null) {
                        dictionaryBuilder = ExternalDictionaryFactory.getDictionaryBuilderByLocale(
                                currentKeyboard.getDefaultDictionaryLocale(), getApplicationContext());
                    } else {
                        Log.d(TAG, "Default dictionary '%s' for keyboard '%s' has been overridden to '%s'",
                                defaultDictionary, currentKeyboard.getKeyboardPrefId(), dictionaryValue);
                        dictionaryBuilder = ExternalDictionaryFactory.getDictionaryBuilderById(dictionaryValue, getApplicationContext());
                    }

                    mSuggest.setMainDictionary(getApplicationContext(), dictionaryBuilder);
                    String localeForSupportingDictionaries = dictionaryBuilder != null ?
                            dictionaryBuilder.getLanguage() : defaultDictionary;
                    mUserDictionary = mSuggest.getDictionaryFactory().createUserDictionary(getApplicationContext(), localeForSupportingDictionaries);
                    mSuggest.setUserDictionary(mUserDictionary);

                    mAutoDictionary = mSuggest.getDictionaryFactory().createAutoDictionary(getApplicationContext(), localeForSupportingDictionaries);
                    mSuggest.setAutoDictionary(mAutoDictionary);
                    mSuggest.setContactsDictionary(getApplicationContext(), mAskPrefs.useContactsDictionary());
                }
            }
        }
    }

    private void launchSettings() {
        handleClose();
        Intent intent = new Intent();
        intent.setClass(AnySoftKeyboard.this, MainSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void launchDictionaryOverriding() {
        final String dictionaryOverridingKey = getDictionaryOverrideKey(getCurrentKeyboard());
        final String dictionaryOverrideValue = mPrefs.getString(
                dictionaryOverridingKey, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(getResources().getString(
                R.string.override_dictionary_title,
                getCurrentKeyboard().getKeyboardName()));
        builder.setNegativeButton(android.R.string.cancel, null);
        ArrayList<CharSequence> dictionaryIds = new ArrayList<>();
        ArrayList<CharSequence> dictionaries = new ArrayList<>();
        // null dictionary is handled as the default for the keyboard
        dictionaryIds.add(null);
        final String SELECTED = "\u2714 ";
        final String NOT_SELECTED = "- ";
        if (dictionaryOverrideValue == null)
            dictionaries.add(SELECTED + getString(R.string.override_dictionary_default));
        else
            dictionaries.add(NOT_SELECTED + getString(R.string.override_dictionary_default));
        // going over all installed dictionaries
        for (DictionaryAddOnAndBuilder dictionaryBuilder : ExternalDictionaryFactory
                .getAllAvailableExternalDictionaries(getApplicationContext())) {
            dictionaryIds.add(dictionaryBuilder.getId());
            String description;
            if (dictionaryOverrideValue != null
                    && dictionaryBuilder.getId()
                    .equals(dictionaryOverrideValue))
                description = SELECTED;
            else
                description = NOT_SELECTED;
            description += dictionaryBuilder.getName();
            if (!TextUtils.isEmpty(dictionaryBuilder.getDescription())) {
                description += " (" + dictionaryBuilder.getDescription() + ")";
            }
            dictionaries.add(description);
        }

        final CharSequence[] ids = new CharSequence[dictionaryIds.size()];
        final CharSequence[] items = new CharSequence[dictionaries.size()];
        dictionaries.toArray(items);
        dictionaryIds.toArray(ids);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int position) {
                di.dismiss();
                Editor editor = mPrefs.edit();
                switch (position) {
                    case 0:
                        Log.d(TAG,
                                "Dictionary overriden disabled. User selected default.");
                        editor.remove(dictionaryOverridingKey);
                        showToastMessage(R.string.override_disabled, true);
                        break;
                    default:
                        if ((position < 0) || (position >= items.length)) {
                            Log.d(TAG, "Dictionary override dialog canceled.");
                        } else {
                            CharSequence id = ids[position];
                            String selectedDictionaryId = (id == null) ? null : id
                                    .toString();
                            String selectedLanguageString = items[position]
                                    .toString();
                            Log.d(TAG,
                                    "Dictionary override. User selected "
                                            + selectedLanguageString
                                            + " which corresponds to id "
                                            + ((selectedDictionaryId == null) ? "(null)"
                                            : selectedDictionaryId));
                            editor.putString(dictionaryOverridingKey,
                                    selectedDictionaryId);
                            showToastMessage(
                                    getString(R.string.override_enabled,
                                            selectedLanguageString), true);
                        }
                        break;
                }
                editor.commit();
                setDictionariesForCurrentKeyboard();
            }
        });

        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    private void showOptionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setNegativeButton(android.R.string.cancel, null);
        CharSequence itemSettings = getString(R.string.ime_settings);
        CharSequence itemOverrideDictionary = getString(R.string.override_dictionary);
        CharSequence itemInputMethod = getString(R.string.change_ime);
        builder.setItems(new CharSequence[]{itemSettings,
                        itemOverrideDictionary, itemInputMethod},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        switch (position) {
                            case 0:
                                launchSettings();
                                break;
                            case 1:
                                launchDictionaryOverriding();
                                break;
                            case 2:
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                        .showInputMethodPicker();
                                break;
                        }
                    }
                });
        builder.setTitle(getResources().getString(R.string.ime_name));
        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        // If orientation changed while predicting, commit the change
        if (newConfig.orientation != mOrientation) {

            setInitialCondensedState(newConfig);

            commitTyped(getCurrentInputConnection());
            mOrientation = newConfig.orientation;

            mKeyboardSwitcher.makeKeyboards(true);
            // new WxH. need new object.
            mSentenceSeparators = getCurrentKeyboard().getSentenceSeparators();

            // should it be always on?
            if (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS))
                notifyKeyboardChangeIfNeeded();
        }

        super.onConfigurationChanged(newConfig);
    }

    private void setInitialCondensedState(Configuration newConfig) {
        final String defaultCondensed = mAskPrefs.getInitialKeyboardCondenseState();
        mKeyboardInCondensedMode = CondenseType.None;
        switch (defaultCondensed) {
            case "split_always":
                mKeyboardInCondensedMode = CondenseType.Split;
                break;
            case "split_in_landscape":
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    mKeyboardInCondensedMode = CondenseType.Split;
                else
                    mKeyboardInCondensedMode = CondenseType.None;
                break;
            case "compact_right_always":
                mKeyboardInCondensedMode = CondenseType.CompactToRight;
                break;
            case "compact_left_always":
                mKeyboardInCondensedMode = CondenseType.CompactToLeft;
                break;
        }

        Log.d(TAG, "setInitialCondensedState: defaultCondensed is "
                + defaultCondensed + " and mKeyboardInCondensedMode is "
                + mKeyboardInCondensedMode);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d(TAG, "onSharedPreferenceChanged - key:" + key);
        AnyApplication.requestBackupToCloud();

        final boolean isKeyboardKey = key.startsWith(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX);
        final boolean isDictionaryKey = key.startsWith("dictionary_");
        final boolean isQuickTextKey = key.equals(getString(R.string.settings_key_active_quick_text_key));
        if (isKeyboardKey || isDictionaryKey || isQuickTextKey) {
            mKeyboardSwitcher.makeKeyboards(true);
        }

        loadSettings();

        if (isDictionaryKey
                || key.equals(getString(R.string.settings_key_use_contacts_dictionary))
                || key.equals(getString(R.string.settings_key_auto_dictionary_threshold))) {
            setDictionariesForCurrentKeyboard();
        } else if (key.equals(getString(R.string.settings_key_ext_kbd_bottom_row_key))
                || key.equals(getString(R.string.settings_key_ext_kbd_top_row_key))
                || key.equals(getString(R.string.settings_key_ext_kbd_ext_ketboard_key))
                || key.equals(getString(R.string.settings_key_ext_kbd_hidden_bottom_row_key))
                || key.equals(getString(R.string.settings_key_keyboard_theme_key))
                || key.equals("zoom_factor_keys_in_portrait")
                || key.equals("zoom_factor_keys_in_landscape")
                || key.equals(getString(R.string.settings_key_smiley_icon_on_smileys_key))
                || key.equals(getString(R.string.settings_key_long_press_timeout))
                || key.equals(getString(R.string.settings_key_multitap_timeout))
                || key.equals(getString(R.string.settings_key_default_split_state))) {
            // in some cases we do want to force keyboards recreations
            resetKeyboardView(key.equals(getString(R.string.settings_key_keyboard_theme_key)));
        }
    }

    public void deleteLastCharactersFromInput(int countToDelete) {
        if (countToDelete == 0)
            return;

        final int currentLength = mWord.length();
        boolean shouldDeleteUsingCompletion;
        if (currentLength > 0) {
            shouldDeleteUsingCompletion = true;
            if (currentLength > countToDelete) {
                int deletesLeft = countToDelete;
                while (deletesLeft > 0) {
                    mWord.deleteLast();
                    deletesLeft--;
                }
            } else {
                mWord.reset();
            }
        } else {
            shouldDeleteUsingCompletion = false;
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (mPredictionOn && shouldDeleteUsingCompletion) {
                ic.setComposingText(mWord.getTypedWord()/* mComposing */, 1);
            } else {
                ic.deleteSurroundingText(countToDelete, 0);
            }
        }
    }

    public void showToastMessage(int resId, boolean forShortTime) {
        CharSequence text = getResources().getText(resId);
        showToastMessage(text, forShortTime);
    }

    private void showToastMessage(CharSequence text, boolean forShortTime) {
        int duration = forShortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        Log.v(TAG, "showToastMessage: '" + text + "'. For: "
                + duration);
        Toast.makeText(this.getApplication(), text, duration).show();
    }

    @Override
    public void onLowMemory() {
        Log.w(TAG, "The OS has reported that it is low on memory!. I'll try to clear some cache.");
        mKeyboardSwitcher.onLowMemory();
        super.onLowMemory();
    }

    public boolean promoteToUserDictionary(String word, int frequency) {
        return !mUserDictionary.isValidWord(word) && mUserDictionary.addWord(word, frequency);
    }

    public WordComposer getCurrentWord() {
        return mWord;
    }

    /**
     * Override this to control when the soft input area should be shown to the
     * user. The default implementation only shows the input view when there is
     * no hard keyboard or the keyboard is hidden. If you change what this
     * returns, you will need to call {@link #updateInputViewShown()} yourself
     * whenever the returned value may have changed to have it re-evalauted and
     * applied. This needs to be re-coded for Issue 620
     */
    @Override
    public boolean onEvaluateInputViewShown() {
        Configuration config = getResources().getConfiguration();
        return config.keyboard == Configuration.KEYBOARD_NOKEYS || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
    }

    public void onCancel() {
        hideWindow();
    }

    public void resetKeyboardView(boolean recreateView) {
        handleClose();
        if (mKeyboardSwitcher != null)
            mKeyboardSwitcher.makeKeyboards(true);
        if (recreateView) {
            // also recreate keyboard view
            setInputView(onCreateInputView());
            setCandidatesView(onCreateCandidatesView());
            setCandidatesViewShown(false);
        }
    }

    private void updateShiftStateNow() {
        final InputConnection ic = getCurrentInputConnection();
        EditorInfo ei = getCurrentInputEditorInfo();
        final int caps;
        if (mAutoCap && ic != null && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
            caps = ic.getCursorCapsMode(ei.inputType);
        } else {
            caps = 0;
        }
        final boolean inputSaysCaps = caps != 0;
        Log.d(TAG, "shift updateShiftStateNow inputSaysCaps=%s", inputSaysCaps);
        mShiftKeyState.setActiveState(inputSaysCaps);
        handleShift();
    }

    /*package*/ void closeDictionaries() {
        if (mSuggest != null) mSuggest.closeDictionaries();
    }
}
