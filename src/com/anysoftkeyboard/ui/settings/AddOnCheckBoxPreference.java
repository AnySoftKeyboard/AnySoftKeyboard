/* The following code was written by Matthew Wiggins 
 * and is released under the APACHE 2.0 license 
 * 
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.anysoftkeyboard.ui.settings;

import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.IconHolder;
import com.anysoftkeyboard.addons.ScreenshotHolder;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.R;

public class AddOnCheckBoxPreference extends Preference implements
		OnCheckedChangeListener, OnClickListener {
	private static final String TAG = "AddOnCheckBoxPreference";

	private CheckBox mCheckBox;
	private TextView mName, mDescription;
	private ImageView mAddOnIcon;
	private View mIconOverlay;
	private AddOn mAddOn;

	public AddOnCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(true);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		LayoutInflater inflator = (LayoutInflater) getContext()
				.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		ViewGroup layout = (ViewGroup) inflator.inflate(
				R.layout.addon_checkbox_pref, null);
		mCheckBox = (CheckBox) layout.findViewById(R.id.addon_checkbox);
		mCheckBox.setOnCheckedChangeListener(this);
		mName = (TextView) layout.findViewById(R.id.addon_title);
		mDescription = (TextView) layout.findViewById(R.id.addon_description);
		mAddOnIcon = (ImageView) layout.findViewById(R.id.addon_image);
		mIconOverlay = layout.findViewById(R.id.addon_image_more_overlay);
		populateViews();
		return layout;
	}

	private void populateViews() {
		if (mAddOn == null || mCheckBox == null)
			return;// view is not ready yet.
		setKey(mAddOn.getId());
		mName.setText(mAddOn.getName());
		mDescription.setText(mAddOn.getDescription());
		Drawable icon = null;
		if (mAddOn instanceof IconHolder) {
			IconHolder addOn = (IconHolder) mAddOn;
			icon = addOn.getIcon();
		}

		if (icon == null) {
			try {
				PackageManager packageManager = getContext().getPackageManager();
				PackageInfo packageInfo = packageManager.getPackageInfo(mAddOn
						.getPackageContext().getPackageName(), 0);
				icon = packageInfo.applicationInfo.loadIcon(packageManager);
			} catch (PackageManager.NameNotFoundException e) {
				icon = null;
				Log.w(TAG,
						"Failed to locate addon package (which is weird, we DID load the addon object from it).");
			}
		}

		mAddOnIcon.setImageDrawable(icon);

		if (mAddOn instanceof ScreenshotHolder) {
			if (((ScreenshotHolder)mAddOn).hasScreenshot()) {
				mAddOnIcon.setOnClickListener(this);
			} else {
				mIconOverlay.setVisibility(View.INVISIBLE);
			}
		}
		boolean defaultChecked = false;
		if (mAddOn instanceof KeyboardAddOnAndBuilder) {
			defaultChecked = ((KeyboardAddOnAndBuilder)mAddOn).getKeyboardDefaultEnabled();
		}
		mCheckBox.setChecked(getPersistedBoolean(defaultChecked));
	}

	public void setAddOn(AddOn addOn) {
		mAddOn = addOn;
		populateViews();
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mCheckBox.setChecked(isChecked);
		persistBoolean(isChecked);
	}

	public void onClick(View view) {
		if (view.getId() == R.id.addon_image) {
			// showing a screenshot (if available)
			Drawable screenshot = null;
			if (mAddOn instanceof ScreenshotHolder) {
				ScreenshotHolder holder = (ScreenshotHolder)mAddOn;
				screenshot = holder.getScreenshot();
			}
			if (screenshot == null) {
				screenshot = mAddOnIcon.getDrawable();
			}
			//
			if (screenshot == null) return;
			//inflating the screenshot view
			LayoutInflater inflator = (LayoutInflater) getContext()
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			ViewGroup layout = (ViewGroup) inflator.inflate(
					R.layout.addon_screenshot, null);
			final PopupWindow popup = new PopupWindow(getContext());
			popup.setContentView(layout);
			DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
			popup.setWidth(dm.widthPixels);
			popup.setHeight(dm.heightPixels);
			popup.setAnimationStyle(R.style.ScreenshotAnimation);
			layout.findViewById(R.id.addon_screenshot_close).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					popup.dismiss();
				}
			});
			((ImageView)layout.findViewById(R.id.addon_screenshot)).setImageDrawable(screenshot);
			popup.showAtLocation(view, Gravity.CENTER, 0, 0);
		}
	}
}
