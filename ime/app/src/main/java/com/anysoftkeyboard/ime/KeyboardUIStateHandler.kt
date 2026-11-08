package com.anysoftkeyboard.ime

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.menny.android.anysoftkeyboard.R
import java.lang.ref.WeakReference

/** handles all kind of UI thread related operations. */
class KeyboardUIStateHandler(keyboard: AnySoftKeyboardSuggestions) :
    Handler(Looper.getMainLooper()) {
  private val keyboardRef = WeakReference(keyboard)

  fun removeAllSuggestionMessages() {
    removeMessages(MSG_UPDATE_SUGGESTIONS)
    removeMessages(MSG_RESTART_NEW_WORD_SUGGESTIONS)
  }

  fun removeAllMessages() {
    removeAllSuggestionMessages()
    removeMessages(MSG_CLOSE_DICTIONARIES)
  }

  override fun handleMessage(msg: Message) {
    val ask = keyboardRef.get() ?: return
    val ic = ask.currentInputConnection

    when (msg.what) {
      MSG_UPDATE_SUGGESTIONS -> ask.performUpdateSuggestions()
      MSG_RESTART_NEW_WORD_SUGGESTIONS -> ask.performRestartWordSuggestion(ic)
      MSG_CLOSE_DICTIONARIES -> ask.closeDictionaries()
      else -> super.handleMessage(msg)
    }
  }

  companion object {
    @JvmField val MSG_UPDATE_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_UPDATE_SUGGESTIONS

    @JvmField
    val MSG_RESTART_NEW_WORD_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_RESTART_NEW_WORD_SUGGESTIONS

    @JvmField val MSG_CLOSE_DICTIONARIES = R.id.keyboard_ui_handler_MSG_CLOSE_DICTIONARIES
  }
}
