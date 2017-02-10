package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.util.Calendar;

public class AboutAnySoftKeyboardFragment extends Fragment {

    private static final String TAG = "AboutAnySoftKeyboardFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_anysoftkeyboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView copyright = (TextView) view.findViewById(R.id.about_copyright);
        copyright.setText(getString(R.string.about_copyright_text, Calendar.getInstance().get(Calendar.YEAR)));

        String appVersionName = "";
        int appVersionNumber = 0;
        try {
            PackageInfo info = view.getContext().getPackageManager().getPackageInfo(view.getContext().getPackageName(), 0);
            appVersionName = info.versionName;
            appVersionNumber = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView version = (TextView) view.findViewById(R.id.about_app_version);
        version.setText(getString(R.string.version_text, appVersionName, appVersionNumber));
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.ime_name);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        setupLink(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                activity.addFragmentToUi(new AdditionalSoftwareLicensesFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
            }
        }, (TextView) getView().findViewById(R.id.about_legal_stuff_link));

        setupLink(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = getString(R.string.privacy_policy);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }, (TextView) getView().findViewById(R.id.about_privacy_link));

        setupLink(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String url = getString(R.string.main_site_url);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }, (TextView) getView().findViewById(R.id.about_web_site_link));
    }

    private void setupLink(ClickableSpan clickHandler, TextView textView) {
        SpannableStringBuilder sb = new SpannableStringBuilder(textView.getText());
        sb.clearSpans();//removing any previously (from instance-state) set click spans.
        sb.setSpan(clickHandler, 0, textView.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(sb);
    }

    public static class AdditionalSoftwareLicensesFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.additional_software_licenses, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            getActivity().setTitle(R.string.about_additional_software_licenses);
        }
    }
}
