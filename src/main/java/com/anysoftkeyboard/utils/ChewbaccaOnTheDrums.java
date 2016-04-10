package com.anysoftkeyboard.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.anysoftkeyboard.base.dictionaries.WordComposer;

import java.util.Random;

public class ChewbaccaOnTheDrums {
    private static final String CHEWBACCAONTHEDRUMS = "chewbacca";
    private static final String DAVIDBOWIE = "davidbowie";

    public static void onKeyTyped(@NonNull WordComposer wordComposer, @NonNull Context applicationContext) {
        CharSequence typed = wordComposer.getTypedWord();
        if (TextUtils.isEmpty(typed)) return;

        String eggType = "";
        if (typed.length() == CHEWBACCAONTHEDRUMS.length() && typed.toString().equals(CHEWBACCAONTHEDRUMS)) {
            eggType = CHEWBACCAONTHEDRUMS;
        } else if (typed.length() == DAVIDBOWIE.length() && typed.toString().equals(DAVIDBOWIE)) {
            eggType = DAVIDBOWIE;
        }

        if (!TextUtils.isEmpty(eggType)) layEgg(eggType, applicationContext);
    }

    private static void layEgg(@NonNull String eggType, @NonNull Context context) {
        Toast.makeText(context, "Check the logcat for a note from AnySoftKeyboard developers!", Toast.LENGTH_LONG).show();

        Log.i("AnySoftKeyboard-"+eggType,
                "*******************"
                        + "\nNICE!!! You found the our easter egg!"
                        + "\nAnySoftKeyboard R&D team would like to thank you for using our keyboard application."
                        + "\nWe hope you enjoying it, we enjoyed making it."
                        + "\n"
                        + "\nThanks."
                        + "\n*******************");

        if (new Random().nextInt(10) <= 2) {
            Intent easterEgg = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getUriForEggType(eggType)));
            easterEgg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(easterEgg);
        }
    }

    @NonNull
    private static String getUriForEggType(@NonNull final String eggType) {
        switch (eggType) {
            case DAVIDBOWIE:
                return "https://open.spotify.com/user/spotify/playlist/7MQd3rOe8kuP2KDjtuiynJ";
            case CHEWBACCAONTHEDRUMS:
                return "https://open.spotify.com/user/official_star_wars/playlist/0uxo0T4OxyGybpsr64CgI1";
            default:
                return "http://evendanan.net";
        }
    }
}
