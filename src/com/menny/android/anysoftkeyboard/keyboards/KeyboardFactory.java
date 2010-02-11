package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.SharedPreferences;
import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardCreatorsFactory.KeyboardCreator;

public class KeyboardFactory 
{
    public static KeyboardCreator[] createAlphaBetKeyboards(AnyKeyboardContextProvider contextProvider)
    {
    	final ArrayList<KeyboardCreator> keyboardCreators = KeyboardCreatorsFactory.getAllCreators(contextProvider);
        Log.i("AnySoftKeyboard", "Creating keyboards. I have "+ keyboardCreators.size()+" creators");
        //Thread.dumpStack();

        //getting shared prefs to determine which to create.
        final SharedPreferences sharedPreferences = contextProvider.getSharedPreferences();
        
        ArrayList<KeyboardCreator> keyboards = new ArrayList<KeyboardCreator>();
        for(int keyboardIndex=0; keyboardIndex<keyboardCreators.size(); keyboardIndex++)
        {
            final KeyboardCreator creator = keyboardCreators.get(keyboardIndex);
            //the first keyboard is defaulted to true
            final boolean keyboardIsEnabled = sharedPreferences.getBoolean(creator.getKeyboardPrefId(), keyboardIndex == 0);

            if (keyboardIsEnabled)
            {
                keyboards.add(creator);
            }
        }

        //sorting the keyboards according to the requested
        //sort order (from minimum to maximum)
        Collections.sort(keyboards, new Comparator<KeyboardCreator>()
                {
            public int compare(KeyboardCreator k1, KeyboardCreator k2) 
            {
                return k1.getSortOrderValue() - k2.getSortOrderValue();
            }
                });

        // Fix: issue 219
        // Check if there is any keyboards created if not, lets create a default english keyboard
        if( keyboards.size() == 0 ) {
            final SharedPreferences.Editor editor = sharedPreferences.edit( );
            final KeyboardCreator creator = keyboardCreators.get( 0 );
            editor.putBoolean( creator.getKeyboardPrefId( ) , true );
            editor.commit( );
            keyboards.add( creator );
        }

        for(final KeyboardCreator aKeyboard : keyboards) {
            Log.d("AnySoftKeyboard", "Factory provided creator: "+aKeyboard.getKeyboardPrefId());
        }

        keyboards.trimToSize();
        final KeyboardCreator[] keyboardsArray = new KeyboardCreator[keyboards.size()];
        return keyboards.toArray(keyboardsArray);
    }

    
}
