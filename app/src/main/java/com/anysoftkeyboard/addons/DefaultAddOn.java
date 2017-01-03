package com.anysoftkeyboard.addons;

import android.content.Context;

import com.menny.android.anysoftkeyboard.R;

/**
 * Empty add-on which is to be used to hold simple implementation for context mapping
 */
public class DefaultAddOn extends AddOnImpl {
    public DefaultAddOn(Context askContext, Context packageContext) {
        super(askContext, packageContext, "DEFAULT_ADD_ON", R.string.default_local_add_on_name, "", false, 0);
    }
}
