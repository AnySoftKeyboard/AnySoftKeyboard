package com.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.res.Resources;

import com.anysoftkeyboard.addons.AddOn;
import com.menny.android.anysoftkeyboard.R;

import java.util.LinkedHashSet;

public class RecentQuickTextKey extends QuickTextKey {
	public RecentQuickTextKey(Context askContext) {
		super(askContext, askContext, "b0316c86-ffa2-49e9-85f7-6cb6e63e18f9", R.string.recent_quick_text_key_name,
				AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID, AddOn.INVALID_RES_ID,
				R.drawable.sym_keyboard_smiley, R.string.quick_text_smiley_key_recent_output, R.string.quick_text_smiley_key_recent_output,
				AddOn.INVALID_RES_ID, askContext.getResources().getString(R.string.recent_quick_text_key_name), 0);
	}

	@Override
	protected String[] getStringArrayFromNamesResId(int popupListNamesResId, Resources resources) {
		String[] names = new String[msLastUsed.size()];
		int index = names.length-1;
		for (RecentKey recentKey : msLastUsed) {
			names[index] = recentKey.name;
			index--;
		}
		return names;
	}

	@Override
	protected String[] getStringArrayFromValuesResId(int popupListValuesResId, Resources resources) {
		String[] values = new String[msLastUsed.size()];
		int index = values.length-1;
		for (RecentKey recentKey : msLastUsed) {
			values[index] = recentKey.value;
			index--;
		}
		return values;
	}

	private static class RecentKey {
		public final String name;
		public final String value;

		private RecentKey(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof RecentKey) return ((RecentKey) o).name.equals(name);
			return false;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	private static final LinkedHashSet<RecentKey> msLastUsed;
	static {
		msLastUsed = new LinkedHashSet<>(20);
		//must have at least one!
		recordUsedKey("\uD83D\uDE03", "\uD83D\uDE03");
	}

	public static void recordUsedKey(String name, String value) {
		RecentKey usedKey = new RecentKey(name, value);
		if (msLastUsed.contains(usedKey)) msLastUsed.remove(usedKey);
		msLastUsed.add(usedKey);
	}
}
