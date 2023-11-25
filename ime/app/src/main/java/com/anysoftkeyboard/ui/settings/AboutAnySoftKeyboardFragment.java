package com.anysoftkeyboard.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;

public class AboutAnySoftKeyboardFragment extends Fragment implements View.OnClickListener {

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.about_anysoftkeyboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    TextView copyright = view.findViewById(R.id.about_copyright);
    copyright.setText(
        getString(R.string.about_copyright_text, Calendar.getInstance().get(Calendar.YEAR)));

    final String appVersionName = BuildConfig.VERSION_NAME;
    final int appVersionNumber = BuildConfig.VERSION_CODE;

    TextView version = view.findViewById(R.id.about_app_version);
    version.setText(getString(R.string.version_text, appVersionName, appVersionNumber));

    view.findViewById(R.id.about_legal_stuff_link).setOnClickListener(this);
    view.findViewById(R.id.about_privacy_link).setOnClickListener(this);
    view.findViewById(R.id.about_web_site_link).setOnClickListener(this);
    view.findViewById(R.id.share_app_details).setOnClickListener(this);
    view.findViewById(R.id.rate_app_in_store).setOnClickListener(this);
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
        Navigation.findNavController(requireView())
            .navigate(
                AboutAnySoftKeyboardFragmentDirections
                    .actionAboutAnySoftKeyboardFragmentToAdditionalSoftwareLicensesFragment());
        break;
      case R.id.about_privacy_link:
        String privacyUrl = getString(R.string.privacy_policy);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)));
        break;
      case R.id.about_web_site_link:
        String siteWebPage = getString(R.string.main_site_url);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(siteWebPage)));
        break;
      case R.id.share_app_details:
        shareAppDetails();
        break;
      case R.id.rate_app_in_store:
        startActivity(
            new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.rate_app_in_store_url, BuildConfig.APPLICATION_ID))));
        break;
      default:
        throw new IllegalArgumentException(
            "Failed to handle " + v.getId() + " in AboutAnySoftKeyboardFragment");
    }
  }

  private void shareAppDetails() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_share_title));
    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_text));
    startActivity(Intent.createChooser(shareIntent, getString(R.string.app_share_menu_title)));
  }

  public static class AdditionalSoftwareLicensesFragment extends Fragment {
    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.additional_software_licenses, container, false);
    }

    @Override
    public void onStart() {
      super.onStart();
      getActivity().setTitle(R.string.about_additional_software_licenses);
    }
  }
}
