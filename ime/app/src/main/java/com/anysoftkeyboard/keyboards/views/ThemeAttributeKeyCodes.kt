package com.anysoftkeyboard.keyboards.views

import androidx.annotation.AttrRes
import com.anysoftkeyboard.api.KeyCodes
import com.menny.android.anysoftkeyboard.R

object ThemeAttributeKeyCodes {
  @JvmStatic
  fun getKeyCodeForIconThemeAttribute(@AttrRes localAttrId: Int): Int =
      when (localAttrId) {
        com.anysoftkeyboard.api.R.attr.iconKeyShift -> KeyCodes.SHIFT
        com.anysoftkeyboard.api.R.attr.iconKeyControl -> KeyCodes.CTRL
        com.anysoftkeyboard.api.R.attr.iconKeyAction -> KeyCodes.ENTER
        com.anysoftkeyboard.api.R.attr.iconKeyBackspace -> KeyCodes.DELETE
        com.anysoftkeyboard.api.R.attr.iconKeyCancel -> KeyCodes.CANCEL
        com.anysoftkeyboard.api.R.attr.iconKeyGlobe -> KeyCodes.MODE_ALPHABET
        com.anysoftkeyboard.api.R.attr.iconKeySpace -> KeyCodes.SPACE
        com.anysoftkeyboard.api.R.attr.iconKeyTab -> KeyCodes.TAB
        com.anysoftkeyboard.api.R.attr.iconKeyArrowDown -> KeyCodes.ARROW_DOWN
        com.anysoftkeyboard.api.R.attr.iconKeyArrowLeft -> KeyCodes.ARROW_LEFT
        com.anysoftkeyboard.api.R.attr.iconKeyArrowRight -> KeyCodes.ARROW_RIGHT
        com.anysoftkeyboard.api.R.attr.iconKeyArrowUp -> KeyCodes.ARROW_UP
        com.anysoftkeyboard.api.R.attr.iconKeyInputMoveHome -> KeyCodes.MOVE_HOME
        com.anysoftkeyboard.api.R.attr.iconKeyInputMoveEnd -> KeyCodes.MOVE_END
        com.anysoftkeyboard.api.R.attr.iconKeyMic -> KeyCodes.VOICE_INPUT
        com.anysoftkeyboard.api.R.attr.iconKeySettings -> KeyCodes.SETTINGS
        com.anysoftkeyboard.api.R.attr.iconKeyCondenseNormal -> KeyCodes.MERGE_LAYOUT
        com.anysoftkeyboard.api.R.attr.iconKeyCondenseSplit -> KeyCodes.SPLIT_LAYOUT
        com.anysoftkeyboard.api.R.attr.iconKeyCondenseCompactToRight ->
            KeyCodes.COMPACT_LAYOUT_TO_RIGHT
        com.anysoftkeyboard.api.R.attr.iconKeyCondenseCompactToLeft ->
            KeyCodes.COMPACT_LAYOUT_TO_LEFT
        com.anysoftkeyboard.api.R.attr.iconKeyClipboardCopy -> KeyCodes.CLIPBOARD_COPY
        com.anysoftkeyboard.api.R.attr.iconKeyClipboardCut -> KeyCodes.CLIPBOARD_CUT
        com.anysoftkeyboard.api.R.attr.iconKeyClipboardPaste -> KeyCodes.CLIPBOARD_PASTE
        com.anysoftkeyboard.api.R.attr.iconKeyClipboardSelect -> KeyCodes.CLIPBOARD_SELECT_ALL
        com.anysoftkeyboard.api.R.attr.iconKeyClipboardFineSelect -> KeyCodes.CLIPBOARD_SELECT
        com.anysoftkeyboard.api.R.attr.iconKeyQuickTextPopup -> KeyCodes.QUICK_TEXT_POPUP
        com.anysoftkeyboard.api.R.attr.iconKeyQuickText -> KeyCodes.QUICK_TEXT
        com.anysoftkeyboard.api.R.attr.iconKeyUndo -> KeyCodes.UNDO
        com.anysoftkeyboard.api.R.attr.iconKeyRedo -> KeyCodes.REDO
        com.anysoftkeyboard.api.R.attr.iconKeyForwardDelete -> KeyCodes.FORWARD_DELETE
        com.anysoftkeyboard.api.R.attr.iconKeyImageInsert -> KeyCodes.IMAGE_MEDIA_POPUP
        com.anysoftkeyboard.api.R.attr.iconKeyClearQuickTextHistory ->
            KeyCodes.CLEAR_QUICK_TEXT_HISTORY
        else -> 0
      }
}
