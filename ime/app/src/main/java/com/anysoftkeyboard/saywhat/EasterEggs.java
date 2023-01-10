package com.anysoftkeyboard.saywhat;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EasterEggs {
    public static List<PublicNotice> create() {
        return Arrays.asList(new ChewbaccaEasterEgg(), new DavidBowieEasterEgg());
    }

    private static class ChewbaccaEasterEgg extends OnKeyEasterEggBaseImpl {

        private ChewbaccaEasterEgg() {
            super(
                    "chewbacca",
                    "https://open.spotify.com/user/official_star_wars/playlist/0uxo0T4OxyGybpsr64CgI1",
                    "WAGRRRRWWGAHHHHWWWRRGGAWWWWWWRR",
                    android.R.drawable.star_on);
        }
    }

    private static class DavidBowieEasterEgg extends OnKeyEasterEggBaseImpl {
        private static final Random RANDOM = new Random();
        private static final String[] LYRICS =
                new String[] {
                    "For here am I sitting in a tin can\nFar above the world\nPlanet Earth is blue\nAnd there's nothing I can do.",
                    "Put on your red shoes\nAnd dance the blues.",
                    "I'm the space invader"
                };

        private DavidBowieEasterEgg() {
            super(
                    "bowie",
                    "https://open.spotify.com/playlist/37i9dQZF1DZ06evO0auErC",
                    () -> LYRICS[RANDOM.nextInt(LYRICS.length)],
                    android.R.drawable.ic_media_play);
        }
    }
}
