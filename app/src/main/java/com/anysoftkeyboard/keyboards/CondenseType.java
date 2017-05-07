package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.api.KeyCodes;

public enum CondenseType {
    None,
    Split,
    CompactToRight,
    CompactToLeft;

    public static CondenseType fromKeyCode(int primaryCode) {
        switch (primaryCode) {
            case KeyCodes.SPLIT_LAYOUT:
                return CondenseType.Split;
            case KeyCodes.MERGE_LAYOUT:
                return CondenseType.None;
            case KeyCodes.COMPACT_LAYOUT_TO_RIGHT:
                return CondenseType.CompactToRight;
            case KeyCodes.COMPACT_LAYOUT_TO_LEFT:
                return CondenseType.CompactToLeft;
            default:
                throw new IllegalArgumentException("Unknown primary code for condenseType: " + primaryCode);
        }
    }
}
