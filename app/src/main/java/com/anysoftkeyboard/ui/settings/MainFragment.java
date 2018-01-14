package com.anysoftkeyboard.ui.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.graphics.Palette;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.ui.settings.setup.SetUpKeyboardWizardFragment;
import com.anysoftkeyboard.ui.settings.setup.SetupSupport;
import com.anysoftkeyboard.ui.tutorials.ChangeLogFragment;
import com.anysoftkeyboard.base.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.lang.ref.WeakReference;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private AnimationDrawable mNotConfiguredAnimation = null;
    private AsyncTask<Bitmap, Void, Palette.Swatch> mPaletteTask;
    private DemoAnyKeyboardView mDemoAnyKeyboardView;

    public static void setupLink(View root, int showMoreLinkId, ClickableSpan clickableSpan, boolean reorderLinkToLastChild) {
        TextView clickHere = root.findViewById(showMoreLinkId);
        if (reorderLinkToLastChild) {
            ViewGroup rootContainer = (ViewGroup) root;
            rootContainer.removeView(clickHere);
            rootContainer.addView(clickHere);
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(clickHere.getText());
        sb.clearSpans();//removing any previously (from instance-state) set click spans.
        sb.setSpan(clickableSpan, 0, clickHere.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            //I to prevent leaks and duplicate ID errors, I must use the getChildFragmentManager
            //to add the inner fragments into the UI.
            //See: https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/285
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.change_log_fragment, new ChangeLogFragment.CardedChangeLogFragment())
                    .commit();
        }
        View testingView = view.findViewById(R.id.testing_build_message);
        testingView.setVisibility(BuildConfig.TESTING_BUILD ? View.VISIBLE : View.GONE);
        mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem aboutMenuItem = menu.add(0, R.id.about_menu_option, Menu.FIRST, R.string.menu_about_item)
                .setIcon(R.drawable.ic_menu_action_about)
                .setVisible(true);
        MenuItemCompat.setShowAsAction(aboutMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_menu_option:
                FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                activity.addFragmentToUi(new AboutAnySoftKeyboardFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        //I'm doing the setup of the link in onViewStateRestored, since the links will be restored too
        //and they will probably refer to a different scoop (Fragment).
        //setting up the underline and click handler in the keyboard_not_configured_box layout
        TextView clickHere = getView().findViewById(R.id.not_configured_click_here);
        mNotConfiguredAnimation = clickHere.getVisibility() == View.VISIBLE ?
                (AnimationDrawable) clickHere.getCompoundDrawables()[0] : null;

        String fullText = getString(R.string.not_configured_with_click_here);
        String justClickHereText = getString(R.string.not_configured_with_just_click_here);
        SpannableStringBuilder sb = new SpannableStringBuilder(fullText);
        // Get the index of "click here" string.
        int start = fullText.indexOf(justClickHereText);
        int length = justClickHereText.length();
        if (start == -1) {
            //this could happen when the localization is not correct
            start = 0;
            length = fullText.length();
        }
        ClickableSpan csp = new ClickableSpan() {
            @Override
            public void onClick(View v) {
                FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                activity.addFragmentToUi(new SetUpKeyboardWizardFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            }
        };
        sb.setSpan(csp, start, start + length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);

        ClickableSpan gplusLink = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.main_site_url)));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException weirdException) {
                    //https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
                    //this means that there is nothing on the device
                    //that can handle Intent.ACTION_VIEW with "https" schema..
                    //silently swallowing it
                    Logger.w(TAG, "Can not open '%' since there is nothing on the device that can handle it.", browserIntent.getData());
                }
            }
        };
        setupLink(getView(), R.id.ask_gplus_link, gplusLink, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.how_to_pointer_title));

        View notConfiguredBox = getView().findViewById(R.id.not_configured_click_here);
        //checking if the IME is configured
        final Context context = getActivity().getApplicationContext();

        if (SetupSupport.isThisKeyboardSetAsDefaultIME(context)) {
            notConfiguredBox.setVisibility(View.GONE);
        } else {
            notConfiguredBox.setVisibility(View.VISIBLE);
        }

        AnyKeyboard defaultKeyboard = AnyApplication.getKeyboardFactory(getContext()).getEnabledAddOn().createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens());
        mDemoAnyKeyboardView.setKeyboard(defaultKeyboard, null, null);

        mPaletteTask = new BitmapPaletteAsyncTask(this);

        mDemoAnyKeyboardView.startPaletteTask(mPaletteTask);

        if (mNotConfiguredAnimation != null)
            mNotConfiguredAnimation.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPaletteTask.cancel(false);
        mPaletteTask = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDemoAnyKeyboardView.onViewNotRequired();
    }

    private static class BitmapPaletteAsyncTask extends AsyncTask<Bitmap, Void, Palette.Swatch> {
        private final WeakReference<Fragment> mFragment;

        private BitmapPaletteAsyncTask(Fragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected Palette.Swatch doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            Palette p = Palette.from(bitmap).generate();
            Palette.Swatch highestSwatch = null;
            for (Palette.Swatch swatch : p.getSwatches()) {
                if (highestSwatch == null || highestSwatch.getPopulation() < swatch.getPopulation())
                    highestSwatch = swatch;
            }
            return highestSwatch;
        }

        @Override
        protected void onPostExecute(Palette.Swatch swatch) {
            super.onPostExecute(swatch);
            final Fragment fragment = mFragment.get();
            if (fragment != null && !isCancelled()) {
                final View rootView = fragment.getView();
                if (swatch != null && rootView != null) {
                    final int backgroundRed = Color.red(swatch.getRgb());
                    final int backgroundGreed = Color.green(swatch.getRgb());
                    final int backgroundBlue = Color.blue(swatch.getRgb());
                    final int backgroundColor = Color.argb(200/*~80% alpha*/, backgroundRed, backgroundGreed, backgroundBlue);
                    TextView gplusLink = rootView.findViewById(R.id.ask_gplus_link);
                    gplusLink.setTextColor(swatch.getTitleTextColor());
                    gplusLink.setBackgroundColor(backgroundColor);
                }
            }
        }
    }
}