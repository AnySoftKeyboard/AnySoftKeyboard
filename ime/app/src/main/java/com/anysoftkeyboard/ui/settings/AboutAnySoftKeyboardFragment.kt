package com.anysoftkeyboard.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.menny.android.anysoftkeyboard.BuildConfig
import com.menny.android.anysoftkeyboard.R
import java.util.Calendar

class AboutAnySoftKeyboardFragment : Fragment(), View.OnClickListener {

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View? = inflater.inflate(R.layout.about_anysoftkeyboard, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val copyright = view.findViewById<TextView>(R.id.about_copyright)
    copyright.text =
        getString(R.string.about_copyright_text, Calendar.getInstance().get(Calendar.YEAR))

    val appVersionName = BuildConfig.VERSION_NAME
    val appVersionNumber = BuildConfig.VERSION_CODE

    val version = view.findViewById<TextView>(R.id.about_app_version)
    version.text = getString(R.string.version_text, appVersionName, appVersionNumber)

    view.findViewById<View>(R.id.about_legal_stuff_link).setOnClickListener(this)
    view.findViewById<View>(R.id.about_privacy_link).setOnClickListener(this)
    view.findViewById<View>(R.id.about_web_site_link).setOnClickListener(this)
    view.findViewById<View>(R.id.share_app_details).setOnClickListener(this)
    view.findViewById<View>(R.id.rate_app_in_store).setOnClickListener(this)
  }

  override fun onStart() {
    super.onStart()
    requireActivity().setTitle(R.string.ime_name)
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.about_legal_stuff_link ->
          Navigation.findNavController(requireView())
              .navigate(
                  AboutAnySoftKeyboardFragmentDirections
                      .actionAboutAnySoftKeyboardFragmentToAdditionalSoftwareLicensesFragment(),
              )

      R.id.about_privacy_link -> {
        val privacyUrl = getString(R.string.privacy_policy)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
      }

      R.id.about_web_site_link -> {
        val siteWebPage = getString(R.string.main_site_url)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(siteWebPage)))
      }

      R.id.share_app_details -> shareAppDetails()

      R.id.rate_app_in_store ->
          startActivity(
              Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(getString(R.string.rate_app_in_store_url, BuildConfig.APPLICATION_ID)),
              ),
          )

      else ->
          throw IllegalArgumentException(
              "Failed to handle ${v.id} in AboutAnySoftKeyboardFragment",
          )
    }
  }

  private fun shareAppDetails() {
    val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_share_title))
          putExtra(Intent.EXTRA_TEXT, getString(R.string.app_share_text))
        }
    startActivity(Intent.createChooser(shareIntent, getString(R.string.app_share_menu_title)))
  }

  class AdditionalSoftwareLicensesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.additional_software_licenses, container, false)

    override fun onStart() {
      super.onStart()
      requireActivity().setTitle(R.string.about_additional_software_licenses)
    }
  }
}
