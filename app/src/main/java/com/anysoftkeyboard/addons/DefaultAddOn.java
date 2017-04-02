package com.anysoftkeyboard.addons;

import android.content.Context;

/**
 * Empty add-on which is to be used to hold simple implementation for context mapping
 */
public class DefaultAddOn extends AddOnImpl {
    public DefaultAddOn(Context askContext, Context packageContext) {
        super(askContext, packageContext, "DEFAULT_ADD_ON", "Local Default Add-On", "", false, 0);
    }
}
