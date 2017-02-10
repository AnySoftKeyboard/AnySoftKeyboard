package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.util.Calendar;

public class AboutAnySoftKeyboardFragment extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_anysoftkeyboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView copyright = (TextView) view.findViewById(R.id.about_copyright);
        copyright.setText(getString(R.string.about_copyright_text, Calendar.getInstance().get(Calendar.YEAR)));

        final String appVersionName = BuildConfig.VERSION_NAME;
        final int appVersionNumber = BuildConfig.VERSION_CODE;
        
        TextView version = (TextView) view.findViewById(R.id.about_app_version);
        version.setText(getString(R.string.version_text, appVersionName, appVersionNumber));

        getView().findViewById(R.id.about_legal_stuff_link).setOnClickListener(this);
        getView().findViewById(R.id.about_privacy_link).setOnClickListener(this);
        getView().findViewById(R.id.about_web_site_link).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.ime_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_legal_stuff_link:
                FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                activity.addFragmentToUi(new AdditionalSoftwareLicensesFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                break;
            case R.id.about_privacy_link:
                String privacyUrl = getString(R.string.privacy_policy);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)));
                break;
            case R.id.about_web_site_link:
                String siteWebPage = getString(R.string.main_site_url);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(siteWebPage)));
                break;
        }
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
