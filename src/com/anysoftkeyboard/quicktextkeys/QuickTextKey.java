package com.anysoftkeyboard.quicktextkeys;

import com.anysoftkeyboard.addons.AddOnImpl;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

/**
 *
 * @author Malcolm
 */
public class QuickTextKey extends AddOnImpl {
	
	private int mPopupKeyboardResId;
	private String[] mPopupListNames;
	private String[] mPopupListValues;
	private int[] mPopupListIconResIds;
	private String mKeyOutputText;

	private String mKeyLabel;
	private int mKeyIconResId;
	private int mIconPreviewResId;

	public QuickTextKey(Context packageContext, String id, int nameResId, int popupKeyboardResId,
			int popupListNamesResId, int popupListValuesResId, int popupListIconsResId,
			int keyIconResId, int keyLabelResId, int keyOutputTextResId, int iconPreviewResId,
			String description, int sortIndex) {
		super(packageContext, id, nameResId, description, sortIndex);
		
		Resources resources = packageContext.getResources();

		this.mPopupKeyboardResId = popupKeyboardResId;
		if (popupKeyboardResId <= 0) {
			this.mPopupListNames = resources.getStringArray(popupListNamesResId);
			this.mPopupListValues = resources.getStringArray(popupListValuesResId);

			if (popupListIconsResId > 0) {
				TypedArray arr = resources.obtainTypedArray(popupListIconsResId);
				mPopupListIconResIds = new int[arr.length()];
				for (int pos = 0; pos < mPopupListIconResIds.length; pos++) {
					mPopupListIconResIds[pos] = arr.getResourceId(pos, -1);
				}
				arr.recycle();
			}
		}
		this.mKeyIconResId = keyIconResId;
		this.mKeyLabel = keyLabelResId > 0 ? resources.getString(keyLabelResId) : null;
		this.mKeyOutputText = keyOutputTextResId > 0 ? resources.getString(keyOutputTextResId) : null;
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

	public int[] getPopupListIconResIds() {
		return mPopupListIconResIds;
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