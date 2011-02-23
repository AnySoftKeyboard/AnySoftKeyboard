package com.menny.android.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.res.Resources;

/**
 *
 * @author Malcolm
 */
public class QuickTextKey {
	private Context mPackageContext;

	private String mId;
	private String mName;

	private int mPopupKeyboardResId;
	private String[] mPopupListNames;
	private String[] mPopupListValues;
	private String mKeyOutputText;

	private String mKeyLabel;
	private int mKeyIconResId;
	private int mIconPreviewResId;

	private final String mDescription;

	public QuickTextKey(Context packageContext, String id, int nameResId, int popupKeyboardResId,
			int popupListNamesResId, int popupListValuesResId, int keyIconResId, int keyLabelResId,
			int keyOutputTextResId, int iconPreviewResId, String description) {
		this.mPackageContext = packageContext;
		Resources resources = packageContext.getResources();

		this.mId = id;
		this.mName = resources.getString(nameResId);

		this.mPopupKeyboardResId = popupKeyboardResId;
		if (popupKeyboardResId <= 0) {
			this.mPopupListNames = resources.getStringArray(popupListNamesResId);
			this.mPopupListValues = resources.getStringArray(popupListValuesResId);
		}
		this.mKeyIconResId = keyIconResId;
		this.mKeyLabel = keyLabelResId > 0 ? resources.getString(keyLabelResId) : null;
		this.mKeyOutputText = keyOutputTextResId > 0 ? resources.getString(keyOutputTextResId) :
			null;
		this.mIconPreviewResId = iconPreviewResId;
		this.mDescription = description;
	}

	public Context getPackageContext() {
		return mPackageContext;
	}

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public boolean isPopupKeyboardUsed() {
		return mPopupKeyboardResId > 0;
	}

	public int getPopupKeyboardResId() {
		return mPopupKeyboardResId;
	}

	public String[] getPopupListNames() {
		return mPopupListNames;
	}

	public String[] getPopupListValues() {
		return mPopupListValues;
	}

	public String getKeyOutputText() {
		return mKeyOutputText;
	}

	public String getKeyLabel() {
		return mKeyLabel;
	}

	public int getKeyIconResId() {
		return mKeyIconResId;
	}

	public int getIconPreviewResId() {
		return mIconPreviewResId;
	}

	public String getDescription() {
		return mDescription;
	}
}