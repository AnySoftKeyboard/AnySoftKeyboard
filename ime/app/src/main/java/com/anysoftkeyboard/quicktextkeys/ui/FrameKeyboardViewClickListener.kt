package com.anysoftkeyboard.quicktextkeys.ui

import android.content.Intent
import android.net.Uri
import android.view.View
import com.anysoftkeyboard.api.KeyCodes
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener
import com.anysoftkeyboard.ui.settings.MainSettingsActivity
import com.menny.android.anysoftkeyboard.R

internal class FrameKeyboardViewClickListener(
    private val keyboardActionListener: OnKeyboardActionListener,
) : View.OnClickListener {

  override fun onClick(v: View) {
    when (v.id) {
      R.id.quick_keys_popup_close ->
          keyboardActionListener.onKey(KeyCodes.CANCEL, null, 0, null, true)

      R.id.quick_keys_popup_backspace ->
          keyboardActionListener.onKey(KeyCodes.DELETE, null, 0, null, true)

      R.id.quick_keys_popup_quick_keys_insert_media ->
          keyboardActionListener.onKey(KeyCodes.IMAGE_MEDIA_POPUP, null, 0, null, true)

      R.id.quick_keys_popup_delete_recently_used_smileys -> {
        keyboardActionListener.onKey(KeyCodes.CLEAR_QUICK_TEXT_HISTORY, null, 0, null, true)
        // re-show
        keyboardActionListener.onKey(KeyCodes.QUICK_TEXT_POPUP, null, 0, null, true)
      }

      R.id.quick_keys_popup_quick_keys_settings -> {
        val startSettings =
            Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(v.context.getString(R.string.deeplink_url_quick_text)),
                    v.context,
                    MainSettingsActivity::class.java,
                )
                .apply {
                  flags =
                      Intent.FLAG_ACTIVITY_NEW_TASK or
                          Intent.FLAG_ACTIVITY_NO_HISTORY or
                          Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                }
        v.context.startActivity(startSettings)
        // and closing keyboard
        keyboardActionListener.onKey(KeyCodes.CANCEL, null, 0, null, true)
      }

      else ->
          throw IllegalArgumentException(
              "Failed to handle view id ${v.id} in FrameKeyboardViewClickListener",
          )
    }
  }

  fun registerOnViews(rootView: View) {
    rootView.findViewById<View>(R.id.quick_keys_popup_close).setOnClickListener(this)
    rootView.findViewById<View>(R.id.quick_keys_popup_backspace).setOnClickListener(this)
    rootView.findViewById<View>(R.id.quick_keys_popup_quick_keys_settings).setOnClickListener(this)
    rootView
        .findViewById<View>(R.id.quick_keys_popup_quick_keys_insert_media)
        .setOnClickListener(this)
    rootView
        .findViewById<View>(R.id.quick_keys_popup_delete_recently_used_smileys)
        .setOnClickListener(this)
  }
}
