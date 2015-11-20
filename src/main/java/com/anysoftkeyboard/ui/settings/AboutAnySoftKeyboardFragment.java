package com.anysoftkeyboard.ui.settings;

import android.content.ActivityNotFoundException;
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

import com.anysoftkeyboard.utils.Log;
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
        TextView version = (TextView)view.findViewById(R.id.about_app_version);
        version.setText(getString(R.string.version_text, appVersionName, appVersionNumber));

        view.findViewById(R.id.about_donate_paypal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=KDYBGNUNMMN94&lc=US&item_name=AnySoftKeyboard&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
                try {
                    getActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    //this means that there is nothing on the device
                    //that can handle Intent.ACTION_VIEW with "https" schema..
                    //silently swallowing it
                    Log.w(TAG, "Can not open '%' since there is nothing on the device that can handle it.", intent.getData());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.ime_name);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        TextView additionalSoftware = (TextView)getView().findViewById(R.id.about_legal_stuff_link);
        SpannableStringBuilder sb = new SpannableStringBuilder(additionalSoftware.getText());
        sb.clearSpans();//removing any previously (from instance-state) set click spans.
        sb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        FragmentChauffeurActivity activity = (FragmentChauffeurActivity)getActivity();
                        activity.addFragmentToUi(new AdditionalSoftwareLicensesFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                    }
                },
                0, additionalSoftware.getText().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        additionalSoftware.setMovementMethod(LinkMovementMethod.getInstance());
        additionalSoftware.setText(sb);
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
