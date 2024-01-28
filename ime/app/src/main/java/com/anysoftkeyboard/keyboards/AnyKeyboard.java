/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Xml;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.CallSuper;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.ime.AnySoftKeyboardBase;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.views.KeyDrawableStateProvider;
import com.anysoftkeyboard.utils.Workarounds;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AnyKeyboard extends Keyboard {
  private static final String TAG = "ASKAnyKeyboard";
  private static final int STICKY_KEY_OFF = 0;
  private static final int STICKY_KEY_ON = 1;
  private static final int STICKY_KEY_LOCKED = 2;
  static final int[] EMPTY_INT_ARRAY = new int[0];
  private int mShiftState = STICKY_KEY_OFF;
  private int mControlState = STICKY_KEY_OFF;
  private Key mShiftKey;
  private Key mControlKey;
  private EnterKey mEnterKey;
  private boolean mRightToLeftLayout = false; // the "super" ctor will create
  private boolean mTopRowWasCreated;
  private boolean mBottomRowWasCreated;

  private int mGenericRowsHeight = 0;
  // max(generic row widths)
  private int mMaxGenericRowsWidth = 0;

  private KeyboardCondenser mKeyboardCondenser;

  // for popup keyboard
  // note: the context can be from a different package!
  protected AnyKeyboard(
      @NonNull AddOn keyboardAddOn,
      @NonNull Context askContext,
      @NonNull Context context,
      int xmlLayoutResId) {
    // should use the package context for creating the layout
    super(keyboardAddOn, askContext, xmlLayoutResId, KEYBOARD_ROW_MODE_NORMAL);
    // no generic rows in popup
  }

  // for the External
  // note: the context can be from a different package!
  protected AnyKeyboard(
      @NonNull AddOn keyboardAddOn,
      @NonNull Context askContext,
      int xmlLayoutResId,
      @KeyboardRowModeId int mode) {
    // should use the package context for creating the layout
    super(keyboardAddOn, askContext, xmlLayoutResId, mode);
  }

  @Override
  public void loadKeyboard(final KeyboardDimens keyboardDimens) {
    final KeyboardExtension topRowPlugin =
        AnyApplication.getTopRowFactory(mLocalContext).getEnabledAddOn();
    final KeyboardExtension bottomRowPlugin =
        AnyApplication.getBottomRowFactory(mLocalContext).getEnabledAddOn();

    loadKeyboard(keyboardDimens, topRowPlugin, bottomRowPlugin);
  }

  public void loadKeyboard(
      final KeyboardDimens keyboardDimens,
      @NonNull KeyboardExtension topRowPlugin,
      @NonNull KeyboardExtension bottomRowPlugin) {
    super.loadKeyboard(keyboardDimens);

    addGenericRows(keyboardDimens, topRowPlugin, bottomRowPlugin);
    initKeysMembers(mLocalContext, keyboardDimens);
    fixEdgeFlags();
  }

  private void fixEdgeFlags() {
    if (getKeys().isEmpty()) return;
    // some assumptions:
    // 1) the first item in the keys list is at the top of the keyboard
    // 2) the last item is the bottom of the keyboard
    // 3) the first key in every row must be left
    // 4) the last key in every row must be right
    // 5) the keys are ordered from top to bottom, from left to right

    final int topY = getKeys().get(0).y;
    final int bottomY = getKeys().get(getKeys().size() - 1).y;

    Key previousKey = null;
    for (Key key : getKeys()) {
      key.edgeFlags = 0;
      if (key.y == topY) key.edgeFlags |= EDGE_TOP;
      if (key.y == bottomY) key.edgeFlags |= EDGE_BOTTOM;

      if (previousKey == null || previousKey.y != key.y) {
        // new row
        key.edgeFlags |= EDGE_LEFT;
        if (previousKey != null) {
          previousKey.edgeFlags |= EDGE_RIGHT;
        }
      }

      previousKey = key;
    }

    // last key must be edge right
    if (previousKey != null) {
      previousKey.edgeFlags |= EDGE_RIGHT;
    }
  }

  public void onKeyboardViewWidthChanged(int newWidth, int oldWidth) {
    if (oldWidth == 0) oldWidth = mDisplayWidth;
    mDisplayWidth = newWidth;
    final double zoomFactor = ((double) newWidth) / ((double) oldWidth);
    for (Key key : getKeys()) {
      key.width = (int) (zoomFactor * key.width);
      key.x = (int) (zoomFactor * key.x);
    }
  }

  private void initKeysMembers(Context askContext, KeyboardDimens keyboardDimens) {
    List<Integer> foundLanguageKeyIndices = new ArrayList<>();

    List<Key> keys = getKeys();
    for (int keyIndex = 0; keyIndex < keys.size(); keyIndex++) {
      Key key = keys.get(keyIndex);
      if (key.mCodes.length > 0) {
        final int primaryCode = key.getPrimaryCode();
        if (key instanceof AnyKey) {
          switch (primaryCode) {
            case KeyCodes.DELETE:
            case KeyCodes.FORWARD_DELETE:
            case KeyCodes.MODE_ALPHABET:
            case KeyCodes.KEYBOARD_MODE_CHANGE:
            case KeyCodes.KEYBOARD_CYCLE:
            case KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE:
            case KeyCodes.KEYBOARD_REVERSE_CYCLE:
            case KeyCodes.ALT:
            case KeyCodes.MODE_SYMBOLS:
            case KeyCodes.QUICK_TEXT:
            case KeyCodes.DOMAIN:
            case KeyCodes.CANCEL:
            case KeyCodes.CTRL:
            case KeyCodes.SHIFT:
              ((AnyKey) key).mFunctionalKey = true;
              break;
          }
        }

        // detecting LTR languages
        if (mRightToLeftLayout || Workarounds.isRightToLeftCharacter((char) primaryCode)) {
          mRightToLeftLayout = true; // one is enough
        }
        switch (primaryCode) {
          case KeyCodes.QUICK_TEXT:
            if (key instanceof AnyKey) {
              AnyKey anyKey = (AnyKey) key;
              if (anyKey.longPressCode == 0
                  && anyKey.popupResId == 0
                  && TextUtils.isEmpty(anyKey.popupCharacters)) {
                anyKey.longPressCode = KeyCodes.QUICK_TEXT_POPUP;
              }
            }
            break;
          case KeyCodes.DOMAIN:
            key.text = key.label = KeyboardPrefs.getDefaultDomain(askContext);
            key.popupResId = R.xml.popup_domains;
            break;
          case KeyCodes.MODE_ALPHABET:
            if (KeyboardPrefs.alwaysHideLanguageKey(askContext)
                || !AnyApplication.getKeyboardFactory(mLocalContext).hasMultipleAlphabets()) {
              // need to hide this key
              foundLanguageKeyIndices.add(keyIndex);
              Logger.d(TAG, "Found a redundant language key at index %d", keyIndex);
            }
            break;
          default:
            // setting the character label
            if (isAlphabetKey(key) && (key.icon == null)) {
              final boolean labelIsOriginallyEmpty = TextUtils.isEmpty(key.label);
              if (labelIsOriginallyEmpty) {
                final int code = key.mCodes[0];
                // check the ASCII table, everything below 32,
                // is not printable
                if (code > 31 && !Character.isWhitespace(code)) {
                  key.label = new String(new int[] {code}, 0, 1);
                }
              }
            }
        }
      }
    }

    if (!foundLanguageKeyIndices.isEmpty()) {
      int keysRemoved = 0;
      for (int foundIndex = 0; foundIndex < foundLanguageKeyIndices.size(); foundIndex++) {
        final int foundLanguageKeyIndex = foundLanguageKeyIndices.get(foundIndex) - keysRemoved;
        final List<Key> keyList = getKeys();
        AnyKey languageKeyToRemove = (AnyKey) keyList.get(foundLanguageKeyIndex);
        // layout requested that this key should always be shown
        if (languageKeyToRemove.showKeyInLayout == AnyKey.SHOW_KEY_ALWAYS) continue;

        keysRemoved++;

        final int rowY = languageKeyToRemove.y;
        int rowStartIndex;
        int rowEndIndex;
        for (rowStartIndex = foundLanguageKeyIndex; rowStartIndex > 0; rowStartIndex--) {
          if (keyList.get(rowStartIndex - 1).y != rowY) break;
        }
        for (rowEndIndex = foundLanguageKeyIndex + 1; rowEndIndex < keyList.size(); rowEndIndex++) {
          if (keyList.get(rowEndIndex).y != rowY) break;
        }

        final float widthToRemove =
            languageKeyToRemove.width + keyboardDimens.getKeyHorizontalGap();
        final float additionalSpacePerKey =
            widthToRemove
                / ((float) (rowEndIndex - rowStartIndex - 1 /*the key that was removed*/));
        float xOffset = 0f;
        for (int keyIndex = rowStartIndex; keyIndex < rowEndIndex; keyIndex++) {
          final Key keyToModify = keyList.get(keyIndex);
          keyToModify.width = (int) (keyToModify.width + additionalSpacePerKey);
          keyToModify.x = (int) (keyToModify.x + xOffset);
          if (keyIndex == foundLanguageKeyIndex) {
            xOffset -= widthToRemove;
          } else {
            xOffset += additionalSpacePerKey;
          }
        }
        keyList.remove(foundLanguageKeyIndex);
      }
    }

    mKeyboardCondenser = new KeyboardCondenser(askContext, this);
  }

  protected void addGenericRows(
      @NonNull final KeyboardDimens keyboardDimens,
      @NonNull KeyboardExtension topRowPlugin,
      @NonNull KeyboardExtension bottomRowPlugin) {
    final boolean disallowGenericRowsOverride =
        KeyboardPrefs.disallowGenericRowOverride(mLocalContext);
    mGenericRowsHeight = 0;
    if (!mTopRowWasCreated || disallowGenericRowsOverride) {
      Logger.d(TAG, "Top row layout id %s", topRowPlugin.getId());
      GenericRowKeyboard rowKeyboard =
          new GenericRowKeyboard(
              topRowPlugin,
              mLocalContext,
              getKeyboardDimens(),
              isAlphabetKeyboard(),
              mKeyboardMode);
      fixKeyboardDueToGenericRow(rowKeyboard, true);
    }
    if (!mBottomRowWasCreated || disallowGenericRowsOverride) {
      Logger.d(TAG, "Bottom row layout id %s", bottomRowPlugin.getId());
      GenericRowKeyboard rowKeyboard =
          new GenericRowKeyboard(
              bottomRowPlugin,
              mLocalContext,
              getKeyboardDimens(),
              isAlphabetKeyboard(),
              mKeyboardMode);
      if (rowKeyboard.hasNoKeys()) {
        Logger.i(
            TAG,
            "Could not find any rows that match mode %d. Trying again with normal" + " mode.",
            mKeyboardMode);
        rowKeyboard =
            new GenericRowKeyboard(
                bottomRowPlugin,
                mLocalContext,
                getKeyboardDimens(),
                isAlphabetKeyboard(),
                KEYBOARD_ROW_MODE_NORMAL);
      }
      fixKeyboardDueToGenericRow(rowKeyboard, false);
    }
  }

  public abstract boolean isAlphabetKeyboard();

  private void fixKeyboardDueToGenericRow(
      @NonNull GenericRowKeyboard genericRowKeyboard, final boolean isTopRow) {
    final int genericRowsHeight = genericRowKeyboard.getHeight();

    final List<Key> keys = getKeys();
    if (isTopRow) {
      // pushing the originals keys down a bit
      for (Key key : keys) {
        key.y += genericRowsHeight;
      }
    }

    int rowKeyInsertIndex = isTopRow ? 0 : keys.size();
    final int rowKeyYOffset = isTopRow ? 0 : getHeight();
    final List<Key> rowKeys = genericRowKeyboard.getKeys();
    for (Key rowKey : rowKeys) {
      rowKey.y += rowKeyYOffset;
      final int rowWidth = Key.getEndX(rowKey);
      if (rowWidth > mMaxGenericRowsWidth) mMaxGenericRowsWidth = rowWidth;
      keys.add(rowKeyInsertIndex, rowKey);
      rowKeyInsertIndex++;
    }

    mGenericRowsHeight += genericRowsHeight;
  }

  @VisibleForTesting
  static class GenericRowKeyboard extends AnyKeyboard {

    private final boolean mInAlphabetMode;

    GenericRowKeyboard(
        @NonNull KeyboardExtension keyboardExtension,
        @NonNull Context askContext,
        @NonNull KeyboardDimens keyboardDimens,
        boolean inAlphabetMode,
        @KeyboardRowModeId int mode) {
      super(keyboardExtension, askContext, keyboardExtension.getKeyboardResId(), mode);
      mInAlphabetMode = inAlphabetMode;
      loadKeyboard(keyboardDimens);
    }

    @Override
    protected void addGenericRows(
        @NonNull KeyboardDimens keyboardDimens,
        @NonNull KeyboardExtension topRowPlugin,
        @NonNull KeyboardExtension bottomRowPlugin) {
      /*no-op*/
    }

    @Override
    public boolean isAlphabetKeyboard() {
      return mInAlphabetMode;
    }

    @Override
    public String getDefaultDictionaryLocale() {
      return null;
    }

    @Override
    public char[] getSentenceSeparators() {
      return new char[0];
    }

    @NonNull @Override
    public CharSequence getKeyboardName() {
      return "not important";
    }

    @Override
    public int getKeyboardIconResId() {
      return AddOn.INVALID_RES_ID;
    }

    @NonNull @Override
    public String getKeyboardId() {
      return "no-important";
    }

    public boolean hasNoKeys() {
      return getKeys().isEmpty();
    }

    @Override
    protected boolean setupKeyAfterCreation(AnyKey key) {
      if (!super.setupKeyAfterCreation(key)) {
        if (key.popupResId == 0 && mInAlphabetMode) {
          switch (key.getPrimaryCode()) {
            case KeyCodes.MODE_SYMBOLS:
            case KeyCodes.KEYBOARD_MODE_CHANGE:
              key.popupResId = R.xml.ext_symbols;
              key.externalResourcePopupLayout = false;
              return true;
          }
        }
      }
      return false;
    }
  }

  @Override
  public int getHeight() {
    return super.getHeight() + mGenericRowsHeight;
  }

  // minWidth is actually 'total width', see android framework source code
  @Override
  public int getMinWidth() {
    return Math.max(mMaxGenericRowsWidth, super.getMinWidth());
  }

  public abstract String getDefaultDictionaryLocale();

  @NonNull public Locale getLocale() {
    return Locale.ROOT;
  }

  // this function is called from within the super constructor.
  @Override
  protected Key createKeyFromXml(
      @NonNull AddOn.AddOnResourceMapping resourceMapping,
      Context askContext,
      Context keyboardContext,
      Row parent,
      KeyboardDimens keyboardDimens,
      int x,
      int y,
      XmlResourceParser parser) {
    AnyKey key = new AnyKey(resourceMapping, keyboardContext, parent, keyboardDimens, x, y, parser);

    if (key.mCodes.length > 0) {
      final int primaryCode = key.mCodes[0];

      // creating less sensitive keys if required
      switch (primaryCode) {
        case KeyCodes.DISABLED: // disabled
          key.disable();
          break;
        case KeyCodes.ENTER: // enter
          key =
              mEnterKey =
                  new EnterKey(
                      resourceMapping, keyboardContext, parent, keyboardDimens, x, y, parser);
          break;
        case KeyCodes.SHIFT:
          mShiftKey = key; // I want the reference used by the super.
          break;
        case KeyCodes.CTRL:
          mControlKey = key;
          break;
        default:
          // no-op
          break;
      }
    }

    setupKeyAfterCreation(key);

    return key;
  }

  @Override
  @Nullable protected Row createRowFromXml(
      @NonNull AddOn.AddOnResourceMapping resourceMapping,
      Resources res,
      XmlResourceParser parser,
      @KeyboardRowModeId int rowMode) {
    Row aRow = super.createRowFromXml(resourceMapping, res, parser, rowMode);
    if (aRow != null) {
      if ((aRow.rowEdgeFlags & Keyboard.EDGE_TOP) != 0) {
        mTopRowWasCreated = true;
      }
      if ((aRow.rowEdgeFlags & Keyboard.EDGE_BOTTOM) != 0) {
        mBottomRowWasCreated = true;
      }
    }

    return aRow;
  }

  private boolean isAlphabetKey(Key key) {
    return !key.repeatable && key.getPrimaryCode() > 0;
  }

  public boolean isStartOfWordLetter(int keyValue) {
    return Character.isLetter(keyValue);
  }

  public boolean isInnerWordLetter(int keyValue) {
    return isStartOfWordLetter(keyValue)
        || keyValue == BTreeDictionary.QUOTE
        || keyValue == BTreeDictionary.CURLY_QUOTE
        || Character.getType(keyValue) == Character.NON_SPACING_MARK
        || Character.getType(keyValue) == Character.COMBINING_SPACING_MARK;
  }

  public abstract char[] getSentenceSeparators();

  /**
   * This looks at the ime options given by the current editor, to set the appropriate label on the
   * keyboard's enter key (if it has one).
   */
  public void setImeOptions(Resources res, EditorInfo editor) {
    if (mEnterKey == null) {
      return;
    }

    mEnterKey.enable();
  }

  @NonNull public abstract CharSequence getKeyboardName();

  public boolean isLeftToRightLanguage() {
    return !mRightToLeftLayout;
  }

  @DrawableRes
  public abstract int getKeyboardIconResId();

  public boolean setShiftLocked(boolean shiftLocked) {
    if (keyboardSupportShift()) {
      final int initialState = mShiftState;
      if (shiftLocked) {
        mShiftState = STICKY_KEY_LOCKED;
      } else if (mShiftState == STICKY_KEY_LOCKED) {
        mShiftState = STICKY_KEY_ON;
      }

      return initialState != mShiftState;
    }

    return false;
  }

  @Override
  public boolean isShifted() {
    if (keyboardSupportShift()) {
      return mShiftState != STICKY_KEY_OFF;
    } else {
      return false;
    }
  }

  @Override
  public boolean setShifted(boolean shiftState) {
    if (keyboardSupportShift()) {
      final int initialState = mShiftState;
      if (shiftState) {
        if (mShiftState == STICKY_KEY_OFF) { // so it is not LOCKED
          mShiftState = STICKY_KEY_ON;
        }
        // else this is already ON, or at caps lock.
      } else {
        mShiftState = STICKY_KEY_OFF;
      }

      return mShiftState != initialState;
    } else {
      super.setShifted(shiftState);
      return false;
    }
  }

  public boolean keyboardSupportShift() {
    return mShiftKey != null;
  }

  public boolean isShiftLocked() {
    return mShiftState == STICKY_KEY_LOCKED;
  }

  public boolean isControl() {
    if (mControlKey != null) {
      return mControlState != STICKY_KEY_OFF;
    } else {
      return false;
    }
  }

  public boolean setControl(boolean control) {
    if (mControlKey != null) {
      final int initialState = mControlState;
      if (control) {
        if (mControlState == STICKY_KEY_OFF) { // so it is not LOCKED
          mControlState = STICKY_KEY_ON;
        }
        // else this is already ON, or at caps lock.
      } else {
        mControlState = STICKY_KEY_OFF;
      }

      return mControlState != initialState;
    } else {
      return false;
    }
  }

  @CallSuper
  protected boolean setupKeyAfterCreation(AnyKey key) {
    // if the keyboard XML already specified the popup, then no
    // need to override
    if (key.popupResId != 0) {
      return true;
    }

    // filling popup res for external keyboards
    if (key.popupCharacters != null) {
      if (key.popupCharacters.length() > 0) {
        key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
      }
      return true;
    }

    return false;
  }

  @NonNull public abstract String getKeyboardId();

  @KeyboardRowModeId
  public int getKeyboardMode() {
    return mKeyboardMode;
  }

  public void setCondensedKeys(CondenseType condenseType) {
    if (mKeyboardCondenser.setCondensedKeys(condenseType, getKeyboardDimens())) {
      computeNearestNeighbors(); // keyboard has changed, so we need to recompute the
      // neighbors.
    }
  }

  public interface HardKeyboardAction {
    int getKeyCode();

    boolean isAltActive();

    boolean isShiftActive();

    void setNewKeyCode(int keyCode);
  }

  public interface HardKeyboardTranslator {
    /*
     * Gets the current state of the hard keyboard, and may change the
     * output key-code.
     */
    void translatePhysicalCharacter(
        HardKeyboardAction action, AnySoftKeyboardBase ime, int multiTapTimeout);
  }

  public static class AnyKey extends Keyboard.Key {
    public static final int SHOW_KEY_ALWAYS = 0;
    public static final int SHOW_KEY_IF_APPLICABLE = 1;
    public static final int SHOW_KEY_NEVER = 2;
    public CharSequence shiftedKeyLabel;
    public CharSequence hintLabel;
    public int longPressCode;
    @ShowKeyInLayoutType public int showKeyInLayout;
    @NonNull int[] mShiftedCodes = EMPTY_INT_ARRAY;
    private boolean mShiftCodesAlways;
    private boolean mFunctionalKey;
    private boolean mEnabled;
    @NonNull private List<String> mKeyTags = Collections.emptyList();

    public AnyKey(Row row, KeyboardDimens keyboardDimens) {
      super(row, keyboardDimens);
    }

    public AnyKey(
        @NonNull AddOn.AddOnResourceMapping resourceMapping,
        Context keyboardContext,
        Keyboard.Row parent,
        KeyboardDimens keyboardDimens,
        int x,
        int y,
        XmlResourceParser parser) {
      super(resourceMapping, keyboardContext, parent, keyboardDimens, x, y, parser);
      // setting up some defaults
      mEnabled = true;
      mFunctionalKey = false;
      longPressCode = 0;
      shiftedKeyLabel = null;
      hintLabel = null;
      boolean mShiftCodesAlwaysOverride = false;

      final int[] remoteStyleableArrayFromLocal =
          resourceMapping.getRemoteStyleableArrayFromLocal(R.styleable.KeyboardLayout_Key);
      TypedArray a =
          keyboardContext.obtainStyledAttributes(
              Xml.asAttributeSet(parser), remoteStyleableArrayFromLocal);
      int n = a.getIndexCount();
      for (int i = 0; i < n; i++) {
        final int remoteIndex = a.getIndex(i);
        final int localAttrId =
            resourceMapping.getLocalAttrId(remoteStyleableArrayFromLocal[remoteIndex]);

        try {
          switch (localAttrId) {
            case R.attr.shiftedCodes:
              mShiftedCodes = KeyboardSupport.getKeyCodesFromTypedArray(a, remoteIndex);
              break;
            case R.attr.longPressCode:
              longPressCode = a.getInt(remoteIndex, 0);
              break;
            case R.attr.isFunctional:
              mFunctionalKey = a.getBoolean(remoteIndex, false);
              break;
            case R.attr.shiftedKeyLabel:
              shiftedKeyLabel = a.getString(remoteIndex);
              break;
            case R.attr.isShiftAlways:
              mShiftCodesAlwaysOverride = true;
              mShiftCodesAlways = a.getBoolean(remoteIndex, false);
              break;
            case R.attr.hintLabel:
              hintLabel = a.getString(remoteIndex);
              break;
            case R.attr.showInLayout:
              //noinspection WrongConstant
              showKeyInLayout = a.getInt(remoteIndex, SHOW_KEY_ALWAYS);
              break;
            case R.attr.tags:
              String tags = a.getString(remoteIndex);
              if (!TextUtils.isEmpty(tags)) {
                mKeyTags = Arrays.asList(tags.split(","));
              }
              break;
          }
        } catch (Exception e) {
          Logger.w(TAG, "Failed to set data from XML!", e);
          if (BuildConfig.DEBUG) throw e;
        }
      }
      a.recycle();

      // ensuring mCodes and mShiftedCodes are the same size
      if (mShiftedCodes.length != mCodes.length) {
        int[] wrongSizedShiftCodes = mShiftedCodes;
        mShiftedCodes = new int[mCodes.length];
        int i;
        for (i = 0; i < wrongSizedShiftCodes.length && i < mCodes.length; i++) {
          mShiftedCodes[i] = wrongSizedShiftCodes[i];
        }
        for (
        /* starting from where i finished above */ ; i < mCodes.length; i++) {
          final int code = mCodes[i];
          if (Character.isLetter(code)) {
            mShiftedCodes[i] = Character.toUpperCase(code);
          } else {
            mShiftedCodes[i] = code;
          }
        }
      }

      if (!mShiftCodesAlwaysOverride) {
        // if the shift-character is a symbol, we only show it if the SHIFT is pressed,
        // not if the shift is active.
        mShiftCodesAlways =
            mShiftedCodes.length == 0
                || Character.isLetter(mShiftedCodes[0])
                || Character.getType(mShiftedCodes[0]) == Character.NON_SPACING_MARK
                || Character.getType(mShiftedCodes[0]) == Character.COMBINING_SPACING_MARK;
      }

      if (popupCharacters != null && popupCharacters.length() == 0) {
        // If there is a keyboard with no keys specified in
        // popupCharacters
        popupResId = 0;
      }
    }

    @Override
    public int getCodeAtIndex(int index, boolean isShifted) {
      return mCodes.length == 0 ? 0 : isShifted ? mShiftedCodes[index] : mCodes[index];
    }

    public boolean isShiftCodesAlways() {
      return mShiftCodesAlways;
    }

    public void enable() {
      mEnabled = true;
    }

    public void disable() {
      iconPreview = null;
      icon = null;
      label = "  "; // can not use NULL.
      mEnabled = false;
    }

    @Override
    public boolean isInside(int clickedX, int clickedY) {
      return mEnabled && super.isInside(clickedX, clickedY);
    }

    public boolean isFunctional() {
      return mFunctionalKey;
    }

    @Override
    public int[] getCurrentDrawableState(KeyDrawableStateProvider provider) {
      if (mFunctionalKey) {
        if (pressed) {
          return provider.KEY_STATE_FUNCTIONAL_PRESSED;
        } else {
          return provider.KEY_STATE_FUNCTIONAL_NORMAL;
        }
      }
      return super.getCurrentDrawableState(provider);
    }

    @NonNull public List<String> getKeyTags() {
      return mKeyTags;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SHOW_KEY_ALWAYS, SHOW_KEY_IF_APPLICABLE, SHOW_KEY_NEVER})
    public @interface ShowKeyInLayoutType {}
  }

  private static class EnterKey extends AnyKey {

    private final int mOriginalHeight;

    public EnterKey(
        @NonNull AddOn.AddOnResourceMapping resourceMapping,
        Context keyboardContext,
        Row parent,
        KeyboardDimens keyboardDimens,
        int x,
        int y,
        XmlResourceParser parser) {
      super(resourceMapping, keyboardContext, parent, keyboardDimens, x, y, parser);
      mOriginalHeight = this.height;
    }

    @Override
    public void disable() {
      this.height = 0;
      super.disable();
    }

    @Override
    public void enable() {
      this.height = mOriginalHeight;
      super.enable();
    }

    @Override
    public int[] getCurrentDrawableState(KeyDrawableStateProvider provider) {
      if (pressed) {
        return provider.KEY_STATE_ACTION_PRESSED;
      } else {
        return provider.KEY_STATE_ACTION_NORMAL;
      }
    }
  }
}
