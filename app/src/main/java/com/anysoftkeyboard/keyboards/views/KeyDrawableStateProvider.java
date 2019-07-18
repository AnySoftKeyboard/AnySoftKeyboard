package com.anysoftkeyboard.keyboards.views;

public class KeyDrawableStateProvider {

    public final int[] KEY_STATE_NORMAL = {};

    public final int[] KEY_STATE_PRESSED = {android.R.attr.state_pressed};

    public final int[] KEY_STATE_FUNCTIONAL_NORMAL;

    public final int[] KEY_STATE_FUNCTIONAL_PRESSED;

    public final int[] DRAWABLE_STATE_MODIFIER_NORMAL = new int[] {};
    public final int[] DRAWABLE_STATE_MODIFIER_PRESSED = new int[] {android.R.attr.state_pressed};
    public final int[] DRAWABLE_STATE_MODIFIER_LOCKED = new int[] {android.R.attr.state_checked};

    public final int[] DRAWABLE_STATE_ACTION_NORMAL = new int[] {};
    public final int[] DRAWABLE_STATE_ACTION_DONE;
    public final int[] DRAWABLE_STATE_ACTION_SEARCH;
    public final int[] DRAWABLE_STATE_ACTION_GO;

    public final int[] KEY_STATE_ACTION_NORMAL;
    public final int[] KEY_STATE_ACTION_PRESSED;

    public KeyDrawableStateProvider(
            final int keyTypeFunctionAttrId /*R.attr.key_type_function*/,
            final int keyActionAttrId /*R.attr.key_type_action*/,
            final int keyActionTypeDoneAttrId /*R.attr.action_done*/,
            final int keyActionTypeSearchAttrId /*R.attr.action_search*/,
            final int keyActionTypeGoAttrId /*R.attr.action_go*/) {
        KEY_STATE_FUNCTIONAL_NORMAL = new int[] {keyTypeFunctionAttrId};
        KEY_STATE_FUNCTIONAL_PRESSED =
                new int[] {keyTypeFunctionAttrId, android.R.attr.state_pressed};

        DRAWABLE_STATE_ACTION_DONE = new int[] {keyActionTypeDoneAttrId};
        DRAWABLE_STATE_ACTION_SEARCH = new int[] {keyActionTypeSearchAttrId};
        DRAWABLE_STATE_ACTION_GO = new int[] {keyActionTypeGoAttrId};

        KEY_STATE_ACTION_NORMAL = new int[] {keyActionAttrId};
        KEY_STATE_ACTION_PRESSED = new int[] {keyActionAttrId, android.R.attr.state_pressed};
    }
}
