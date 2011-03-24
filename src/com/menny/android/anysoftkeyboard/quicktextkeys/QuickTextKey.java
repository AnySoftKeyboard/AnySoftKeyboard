package com.menny.android.anysoftkeyboard.quicktextkeys;

import com.menny.android.anysoftkeyboard.addons.AddOnImpl;

import android.content.Context;
import android.content.res.Resources;

/**
 *
 * @author Malcolm
 */
public class QuickTextKey extends AddOnImpl {
	
	private int mPopupKeyboardResId;
	private String[] mPopupListNames;
	private String[] mPopupListValues;
	private String mKeyOutputText;

	private String mKeyLabel;
	private int mKeyIconResId;
	private int mIconPreviewResId;

	public QuickTextKey(Context packageContext, String id, int nameResId, int popupKeyboardResId,
			int popupListNamesResId, int popupListValuesResId, int keyIconResId, int keyLabelResId,
			int keyOutputTextResId, int iconPreviewResId, String description, int sortIndex) {
		super(packageContext, id, nameResId, description, sortIndex);
		
		Resources resources = packageContext.getResources();

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
}