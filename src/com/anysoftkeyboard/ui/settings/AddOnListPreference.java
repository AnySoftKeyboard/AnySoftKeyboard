package com.anysoftkeyboard.ui.settings;

import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.IconHolder;
import com.anysoftkeyboard.addons.ScreenshotHolder;
import com.menny.android.anysoftkeyboard.R;

public class AddOnListPreference extends ListPreference {

	private AddOn[] mAddOns;

	public AddOnListPreference(Context context) {
		super(context);
	}

	public AddOnListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		ListAdapter listAdapter = new AddOnArrayAdapter(getContext(),
				R.layout.addon_list_item_pref, mAddOns);

		builder.setAdapter(listAdapter, this);
		super.onPrepareDialogBuilder(builder);
	}

	public void setAddOnsList(AddOn[] addOns) {
		mAddOns = addOns;
		
		String[] ids = new String[mAddOns.length];
		String[] names = new String[mAddOns.length];
		int entryPos = 0;
		for (AddOn addOn : mAddOns) {
			ids[entryPos] = addOn.getId();
			names[entryPos] = addOn.getName();
			entryPos++;
		}
		setEntries(names);
		setEntryValues(ids);
	}

	private class AddOnArrayAdapter extends ArrayAdapter<AddOn> implements
			OnClickListener {

		public AddOnArrayAdapter(Context context, int textViewResourceId,
				AddOn[] objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final AddOn addOn = getItem(position);
			// inflate layout
			LayoutInflater inflator = (LayoutInflater) getContext()
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			View row = inflator.inflate(R.layout.addon_list_item_pref, parent,
					false);
			row.setTag(addOn);

			// set on click listener for row
			row.setOnClickListener(this);

			// set addon details
			TextView title = (TextView) row.findViewById(R.id.addon_title);
			title.setText(addOn.getName());
			TextView description = (TextView) row
					.findViewById(R.id.addon_description);
			description.setText(addOn.getDescription());
			Drawable icon = null;
			if (addOn instanceof IconHolder) {
				IconHolder iconHolder = (IconHolder) addOn;
				icon = iconHolder.getIcon();
			}
			if (icon == null) {
				try {
					PackageManager packageManager = getContext()
							.getPackageManager();
					PackageInfo packageInfo = packageManager.getPackageInfo(
							addOn.getPackageContext().getPackageName(), 0);
					icon = packageInfo.applicationInfo.loadIcon(packageManager);
				} catch (PackageManager.NameNotFoundException e) {
					icon = null;
				}
			}

			ImageView addOnIcon = (ImageView) row
					.findViewById(R.id.addon_image);
			addOnIcon.setImageDrawable(icon);
			if (addOn instanceof ScreenshotHolder) {
				if (((ScreenshotHolder)addOn).hasScreenshot()) {
					addOnIcon.setOnClickListener(this);
					addOnIcon.setTag(addOn);
				} else {
					row.findViewById(R.id.addon_image_more_overlay).setVisibility(View.INVISIBLE);
				}
			}
			
			// set checkbox
			RadioButton tb = (RadioButton) row
					.findViewById(R.id.addon_checkbox);
			if (addOn.getId() == AddOnListPreference.this.getValue()) {
				tb.setChecked(true);
			}
			tb.setClickable(false);

			return row;
		}

		public void onClick(View v) {
			if (v.getId() == R.id.addon_list_item_layout) {
				AddOnListPreference.this.setValue(((AddOn)v.getTag()).getId());
				getDialog().dismiss();
			} else if (v.getId() == R.id.addon_image) {
				// showing a screenshot (if available)
				AddOn addOn = (AddOn)v.getTag();
				Drawable screenshot = null;
				if (addOn instanceof ScreenshotHolder) {
					ScreenshotHolder holder = (ScreenshotHolder)addOn;
					screenshot = holder.getScreenshot();
				}
				if (screenshot == null) {
					screenshot = ((ImageView)v).getDrawable();
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
				popup.showAtLocation(v, Gravity.CENTER, 0, 0);
			}
		}
	}
}
