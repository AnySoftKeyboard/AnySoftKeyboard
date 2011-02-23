package com.menny.android.anysoftkeyboard.quicktextkeys;

import android.content.SharedPreferences;
import android.util.Log;
import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.quicktextkeys.QuickTextKeyBuildersFactory.QuickTextKeyBuilder;

import java.util.ArrayList;

public class QuickTextKeyFactory {
	private static final String TAG = "ASK_QTKF";

    public static QuickTextKeyBuilder getQuickTextKeyBuilder(AnyKeyboardContextProvider
			contextProvider) {
    	ArrayList<QuickTextKeyBuilder> keyCreators = QuickTextKeyBuildersFactory
				.getAllBuilders(contextProvider.getApplicationContext());
		if (keyCreators.isEmpty()) {
			Log.w(TAG, "I haven't found any quick text key plugins!");
			return null;
		} else {
			Log.i(TAG, "Creating quick text keys. I have " + keyCreators.size() + " creators");
		}
        //Thread.dumpStack();

        //Find out which key should be created
		QuickTextKeyBuilder selectedKeyBuilder = null;
        SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
		String settingKey = contextProvider
				.getApplicationContext().getString(R.string.settings_key_active_quick_text_key);
        String selectedKeyId = sharedPreferences.getString(settingKey, null);
		if (selectedKeyId != null) {
			//Find the builder in the array by id. Mayne would've been better off with a HashSet
			for (QuickTextKeyBuilder builder : keyCreators) {
				if (builder.getId().equals(selectedKeyId)) {
					selectedKeyBuilder = builder;
					break;
				}
			}
		}

		if (selectedKeyBuilder == null) {
			//Haven't found a builder or no preference is stored, so we use the default one
			selectedKeyBuilder = keyCreators.get(0);

			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(settingKey, selectedKeyBuilder.getId());
			editor.commit();
		}

        if (AnySoftKeyboardConfiguration.DEBUG) {
			Log.d(TAG, "List of available quick text keys:");
	        for (QuickTextKeyBuilder builder : keyCreators) {
				//If it weren'r an interface, I'd rather do the same thing through toString()
				Log.d(TAG, builder.getId() + " " + builder.getDescription());
			}
        }

        return selectedKeyBuilder;
    }
}