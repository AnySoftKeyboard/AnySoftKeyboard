package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.settings.setup.SetUpKeyboardWizardFragment;
import com.anysoftkeyboard.ui.settings.setup.SetupSupport;
import com.anysoftkeyboard.ui.tutorials.ChangeLogFragment;
import com.anysoftkeyboard.ui.tutorials.TipsFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            //until google fixes the problem with nested fragments, I'll need to add the fragment by code
            //The problem is that if I want to define the fragment in XML, and have an ID associated to it,
            //it will fail (since the fragment is nested, and during inflation it is unknown to the inflater).
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.random_tip_fragment_container, new TipsFragment.RandomTipFragment())
                    .commit();
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //I'm doing the setup of the link in onViewStateRestored, since the links will be restored too
        //and they will probably refer to a different scoop (Fragment).
        //setting up the underline and click handler in the keyboard_not_configured_box layout
        TextView clickHere = (TextView) getView().findViewById(R.id.not_configured_click_here);
        String fullText = getString(R.string.not_configured_with_click_here);
        String justClickHereText = getString(R.string.not_configured_with_just_click_here);
        SpannableStringBuilder sb = new SpannableStringBuilder(fullText);
        // Get the index of "click here" string.
        int start = fullText.indexOf(justClickHereText);
        int length = justClickHereText.length();
        if (start == -1) {
            //this could happen when the localizationis not correct
            start = 0;
            length = fullText.length();
        }
        ClickableSpan csp = new ClickableSpan() {
            @Override
            public void onClick(View v) {
                FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                activity.addFragmentToUi(new SetUpKeyboardWizardFragment(),
                        FragmentChauffeurActivity.FragmentUiContext.ExpandedItem,
                        getView().findViewById(R.id.keyboard_not_configured_box));
            }
        };
        sb.setSpan(csp, start, start + length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);

        //setting up change_log
        setupLink(getView(), R.id.read_more_change_log,
                new ClickableSpan() {
                    @Override
                    public void onClick(View v) {
                        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                        activity.addFragmentToUi(ChangeLogFragment.createFragment(ChangeLogFragment.SHOW_ALL_CHANGELOG),
                                FragmentChauffeurActivity.FragmentUiContext.ExpandedItem,
                                getView().findViewById(R.id.change_log_card));
                    }
                });
        //setting up tips
        setupLink(getView(), R.id.show_more_tips,
                new ClickableSpan() {
                    @Override
                    public void onClick(View v) {
                        TipsFragment.RandomTipFragment fragment =
                                (TipsFragment.RandomTipFragment) MainFragment.this.getChildFragmentManager().findFragmentById(R.id.random_tip_fragment_container);
                        int tipLayoutIdToStartWith = fragment.shownTipLayoutResId();

                        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                        activity.addFragmentToUi(TipsFragment.createFragment(TipsFragment.SHOW_ALL_TIPS, tipLayoutIdToStartWith),
                                FragmentChauffeurActivity.FragmentUiContext.ExpandedItem,
                                getView().findViewById(R.id.tips_card));
                    }
                });

    }

    private void setupLink(View root, int showMoreLinkId, ClickableSpan clickableSpan) {
        TextView clickHere = (TextView) root.findViewById(showMoreLinkId);
        SpannableStringBuilder sb = new SpannableStringBuilder(clickHere.getText());
        sb.clearSpans();//removing any previously (from instance-state) set click spans.
        sb.setSpan(clickableSpan, 0, clickHere.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
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
        final boolean isDefaultIME;
        isDefaultIME = SetupSupport.isThisKeyboardSetAsDefaultIME(context);

        if (isDefaultIME) {
            notConfiguredBox.setVisibility(View.GONE);
        } else {
            notConfiguredBox.setVisibility(View.VISIBLE);
        }

        //updating the keyboard layout to the current theme screen shot (if exists).
        KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(getActivity().getApplicationContext());
        if (theme == null)
            theme = KeyboardThemeFactory.getFallbackTheme(getActivity().getApplicationContext());
        Drawable themeScreenShot = theme.getScreenshot();

        ImageView screenShotHolder = (ImageView) getView().findViewById(R.id.keyboard_screen_shot);
        if (themeScreenShot == null)
            screenShotHolder.setBackgroundResource(R.drawable.lean_dark_theme_screenshot);
        else
            screenShotHolder.setImageDrawable(themeScreenShot);
    }

}