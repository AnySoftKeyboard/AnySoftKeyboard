package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.R;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setting up the underline and click handler in the keyboard_not_configured_box layout
        TextView clickHere = (TextView) view.findViewById(R.id.not_configured_click_here);
        String fullText = getString(R.string.not_configured_with_click_here);
        String justClickHereText = getString(R.string.not_configured_with_just_click_here);
        SpannableStringBuilder sb = new SpannableStringBuilder(fullText);
        // Get the index of "click here" string.
        int start = fullText.indexOf(justClickHereText);
        int length = justClickHereText.length();
        ClickableSpan csp = new ClickableSpan() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), WelcomeHowToNoticeActivity.class);
                startActivity(i);
            }
        };
        sb.setSpan(csp, start, start + length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.how_to_pointer_title));

        View notConfiguredBox = getView().findViewById(R.id.keyboard_not_configured_box);
        //checking if the IME is configured
        final Context context = getActivity().getApplicationContext();
        //checking the default IME
        final String defaultIME = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (TextUtils.isEmpty(defaultIME) || !defaultIME.startsWith(context.getPackageName())) {
            //I'm going to show the warning dialog
            //whenever AnySoftKeyboard is not marked as the default.
            notConfiguredBox.setVisibility(View.VISIBLE);
        } else {
            notConfiguredBox.setVisibility(View.GONE);
        }
    }
}