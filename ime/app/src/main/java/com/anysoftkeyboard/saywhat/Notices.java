package com.anysoftkeyboard.saywhat;

import android.content.Context;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Notices {
  public static List<PublicNotice> create(Context context) {
    return Collections.unmodifiableList(
        Arrays.asList(
            new ThanksgivingNotice(),
            new HalloweenNotice(),
            new NewYearsEveNotice(),
            new NewYearsDayNotice(),
            new PrivacyDayNotice(),
            new EarthDayNotice(),
            new PiDayNotice(),
            new StarWarsDayNotice()));
  }
}
