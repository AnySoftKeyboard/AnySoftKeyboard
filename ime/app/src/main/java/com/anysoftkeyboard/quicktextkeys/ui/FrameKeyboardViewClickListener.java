package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Intent;
import android.view.View;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

/*package*/ class FrameKeyboardViewClickListener implements View.OnClickListener {
    private final OnKeyboardActionListener mKeyboardActionListener;

    public FrameKeyboardViewClickListener(OnKeyboardActionListener keyboardActionListener) {
        mKeyboardActionListener = keyboardActionListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quick_keys_popup_close:
                mKeyboardActionListener.onKey(KeyCodes.CANCEL, null, 0, null, true);
                break;
            case R.id.quick_keys_popup_backspace:
                mKeyboardActionListener.onKey(KeyCodes.DELETE, null, 0, null, true);
                break;
            case R.id.quick_keys_popup_quick_keys_insert_media:
                mKeyboardActionListener.onKey(KeyCodes.IMAGE_MEDIA_POPUP, null, 0, null, true);
                break;
            case R.id.quick_keys_popup_delete_recently_used_smileys:
                mKeyboardActionListener.onKey(
                        KeyCodes.DELETE_RECENT_USED_SMILEYS, null, 0, null, true);
                break;
            case R.id.quick_keys_popup_quick_keys_settings:
                Intent startSettings =
                        FragmentChauffeurActivity.createStartActivityIntentForAddingFragmentToUi(
                                v.getContext(),
                                MainSettingsActivity.class,
                                new QuickTextKeysBrowseFragment(),
                                TransitionExperiences.ROOT_FRAGMENT_EXPERIENCE_TRANSITION);
                startSettings.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_NO_HISTORY
                                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                v.getContext().startActivity(startSettings);
                // and closing keyboard
                mKeyboardActionListener.onKey(KeyCodes.CANCEL, null, 0, null, true);
                break;
            default:
                throw new IllegalArgumentException(
                        "Failed to handle view id "
                                + v.getId()
                                + " in FrameKeyboardViewClickListener");
        }
    }

    void registerOnViews(View rootView) {
        rootView.findViewById(R.id.quick_keys_popup_close).setOnClickListener(this);
        rootView.findViewById(R.id.quick_keys_popup_backspace).setOnClickListener(this);
        rootView.findViewById(R.id.quick_keys_popup_quick_keys_settings).setOnClickListener(this);
        rootView.findViewById(R.id.quick_keys_popup_quick_keys_insert_media)
                .setOnClickListener(this);
        rootView.findViewById(R.id.quick_keys_popup_delete_recently_used_smileys)
                .setOnClickListener(this);
    }
}
