package com.anysoftkeyboard.addons;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import com.anysoftkeyboard.utils.Logger;

import java.util.ArrayList;
import java.util.List;

class Support {
    private static final String TAG = Support.class.getName();

    /**
     * Creates a mapping between the local styleable and the remote.
     * @param localStyleableArray the local styleable to map against
     * @param localContext local APK's Context
     * @param remoteContext remote package's Context
     * @param attributeIdMap a mapping between the remote-id -> local-id
     * @return Always returns the remote version of localStyleableArray
     */
    public static int[] createBackwardCompatibleStyleable(@NonNull int[] localStyleableArray, @NonNull Context localContext, @NonNull Context remoteContext, @NonNull SparseIntArray attributeIdMap) {
        if (localContext == null) throw new NullPointerException("askContext can not be null");
        if (remoteContext == null) throw new NullPointerException("context can not be null");

        final String remotePackageName = remoteContext.getPackageName();
        if (localContext.getPackageName().equals(remotePackageName)) {
            Logger.d(TAG, "This is a local context ("+remotePackageName+"), optimization will be done.");
            //optimization
            for(int attrId : localStyleableArray) {
                attributeIdMap.put(attrId, attrId);
            }
            return localStyleableArray;
        }
        final Resources localRes = localContext.getResources();
        final Resources remoteRes = remoteContext.getResources();
        List<Integer> styleableIdList = new ArrayList<>(localStyleableArray.length);
        for(int attrId : localStyleableArray) {
            final boolean isAndroidAttribute = localRes.getResourcePackageName(attrId).equals("android");
            final int remoteAttrId;

            if (isAndroidAttribute) {
                //android attribute IDs are the same always. So, I can optimize.
                remoteAttrId = attrId;
            } else {
                final String attributeName = localRes.getResourceEntryName(attrId);
                remoteAttrId = remoteRes.getIdentifier(attributeName, "attr", remotePackageName);
                Logger.d(TAG, "attr "+attributeName+", local id "+attrId+", remote id "+remoteAttrId);
            }
            if (remoteAttrId != 0) {
                attributeIdMap.put(remoteAttrId, attrId);
                styleableIdList.add(remoteAttrId);
            }
        }
        final int[] remoteMappedStyleable = new int[styleableIdList.size()];
        for(int i=0; i<remoteMappedStyleable.length; i++) {
            remoteMappedStyleable[i] = styleableIdList.get(i);
        }

        return remoteMappedStyleable;
    }
}
