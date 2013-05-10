/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.evendanan.pushingpixels;

import android.content.Context;
import android.util.AttributeSet;

/**
 * The same as the regular ListPreference, but allows formatting of the summary field.
 * This is not needed if your min-API is Honeycomb (since it's there already).
 */
public class ListPreference extends android.preference.ListPreference {

    public ListPreference(Context context) {
        super(context);
    }

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public CharSequence getSummary() {
        //I need to update the summary, in case it includes a String#Format place-holders.
        //now, what does this code do: In some versions of Android (prior to Honeycomb)
        //the getSummary does include the nifty trick of allowing the developer to
        //use the %s place holder. So, if I include a "%s" in the strings, in Gingerbread it will be printed
        //while in Honeycomb it will be replaced with the current selection.
        //So I hack: If the device is GB, then "getSummary" will include the %s, and the String.format function
        //will replace it. Win!
        //if the device is Honeycomb, then "getSummary" will already replace the %s, and it wont be there, and
        //the String.format function will do nothing! Win again!
        return String.format(super.getSummary().toString(), getEntry());
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        //so the Summary will be updated
        notifyChanged();
    }

}
