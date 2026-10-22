package com.anysoftkeyboard.keyboards.views

import androidx.annotation.AttrRes
import com.anysoftkeyboard.api.KeyCodes
import com.menny.android.anysoftkeyboard.R

object ThemeAttributeKeyCodes {
  @JvmStatic
  fun getKeyCodeForIconThemeAttribute(@AttrRes localAttrId: Int): Int =
      when (localAttrId) {
        R.attr.iconKeyShift -> KeyCodes.SHIFT
        R.attr.iconKeyControl -> KeyCodes.CTRL
        R.attr.iconKeyAction -> KeyCodes.ENTER
        R.attr.iconKeyBackspace -> KeyCodes.DELETE
        R.attr.iconKeyCancel -> KeyCodes.CANCEL
        R.attr.iconKeyGlobe -> KeyCodes.MODE_ALPHABET
        R.attr.iconKeySpace -> KeyCodes.SPACE
        R.attr.iconKeyTab -> KeyCodes.TAB
        R.attr.iconKeyArrowDown -> KeyCodes.ARROW_DOWN
        R.attr.iconKeyArrowLeft -> KeyCodes.ARROW_LEFT
        R.attr.iconKeyArrowRight -> KeyCodes.ARROW_RIGHT
        R.attr.iconKeyArrowUp -> KeyCodes.ARROW_UP
        R.attr.iconKeyInputMoveHome -> KeyCodes.MOVE_HOME
        R.attr.iconKeyInputMoveEnd -> KeyCodes.MOVE_END
        R.attr.iconKeyMic -> KeyCodes.VOICE_INPUT
        R.attr.iconKeySettings -> KeyCodes.SETTINGS
        R.attr.iconKeyCondenseNormal -> KeyCodes.MERGE_LAYOUT
        R.attr.iconKeyCondenseSplit -> KeyCodes.SPLIT_LAYOUT
        R.attr.iconKeyCondenseCompactToRight -> KeyCodes.COMPACT_LAYOUT_TO_RIGHT
        R.attr.iconKeyCondenseCompactToLeft -> KeyCodes.COMPACT_LAYOUT_TO_LEFT
        R.attr.iconKeyClipboardCopy -> KeyCodes.CLIPBOARD_COPY
        R.attr.iconKeyClipboardCut -> KeyCodes.CLIPBOARD_CUT
        R.attr.iconKeyClipboardPaste -> KeyCodes.CLIPBOARD_PASTE
        R.attr.iconKeyClipboardSelect -> KeyCodes.CLIPBOARD_SELECT_ALL
        R.attr.iconKeyClipboardFineSelect -> KeyCodes.CLIPBOARD_SELECT
        R.attr.iconKeyQuickTextPopup -> KeyCodes.QUICK_TEXT_POPUP
        R.attr.iconKeyQuickText -> KeyCodes.QUICK_TEXT
        R.attr.iconKeyUndo -> KeyCodes.UNDO
        R.attr.iconKeyRedo -> KeyCodes.REDO
        R.attr.iconKeyForwardDelete -> KeyCodes.FORWARD_DELETE
        R.attr.iconKeyImageInsert -> KeyCodes.IMAGE_MEDIA_POPUP
        R.attr.iconKeyClearQuickTextHistory -> KeyCodes.CLEAR_QUICK_TEXT_HISTORY
        else -> 0
      }
}
