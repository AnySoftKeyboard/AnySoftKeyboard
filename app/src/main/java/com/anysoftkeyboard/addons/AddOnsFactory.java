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

package com.anysoftkeyboard.addons;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.utils.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public abstract class AddOnsFactory<E extends AddOn> {

    private static final class AddOnsComparator implements Comparator<AddOn> {
        private final String mAskPackageName;

        private AddOnsComparator(Context askContext) {
            mAskPackageName = askContext.getPackageName();
        }

        public int compare(AddOn k1, AddOn k2) {
            String c1 = k1.getPackageName();
            String c2 = k2.getPackageName();

            if (c1.equals(c2))
                return k1.getSortIndex() - k2.getSortIndex();
            else if (c1.equals(mAskPackageName))//I want to make sure ASK packages are first
                return -1;
            else if (c2.equals(mAskPackageName))
                return 1;
            else
                return c1.compareToIgnoreCase(c2);
        }
    }

    private final static ArrayList<AddOnsFactory<?>> mActiveInstances = new ArrayList<>();

    private static final String sTAG = "AddOnsFactory";

    public static void onPackageChanged(final Intent eventIntent, final AnySoftKeyboard ask) {
        boolean cleared = false;
        boolean recreateView = false;
        for (AddOnsFactory<?> factory : mActiveInstances) {
            try {
                if (factory.isEventRequiresCacheRefresh(eventIntent, ask.getApplicationContext())) {
                    cleared = true;
                    if (factory.isEventRequiresViewReset(eventIntent, ask.getApplicationContext())) recreateView = true;
                    Logger.d(sTAG, factory.getClass().getName() + " will handle this package-changed event. Also recreate view? " + recreateView);
                    factory.clearAddOnList();
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (cleared) ask.resetKeyboardView(recreateView);
    }

    public static AddOn locateAddOn(String id, Context askContext) {
        for (AddOnsFactory<?> factory : mActiveInstances) {
            AddOn addOn = factory.getAddOnById(id, askContext);
            if (addOn != null) {
                Logger.d(sTAG, "Located addon with id " + addOn.getId() + " of type " + addOn.getClass().getName());
                return addOn;
            }
        }

        return null;
    }

    protected final String TAG;

    /**
     * This is the interface name that a broadcast receiver implementing an
     * external addon should say that it supports -- that is, this is the
     * action it uses for its intent filter.
     */
    private final String RECEIVER_INTERFACE;

    /**
     * Name under which an external addon broadcast receiver component
     * publishes information about itself.
     */
    private final String RECEIVER_META_DATA;

    private final ArrayList<E> mAddOns = new ArrayList<>();
    private final HashMap<String, E> mAddOnsById = new HashMap<>();

    private final boolean mReadExternalPacksToo;
    private final String ROOT_NODE_TAG;
    private final String ADDON_NODE_TAG;
    private final int mBuildInAddOnsResId;

    private static final String XML_PREF_ID_ATTRIBUTE = "id";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
    private static final String XML_SORT_INDEX_ATTRIBUTE = "index";

    protected AddOnsFactory(String tag, String receiverInterface, String receiverMetaData, String rootNodeTag, String addonNodeTag, int buildInAddonResId, boolean readExternalPacksToo) {
        TAG = tag;
        RECEIVER_INTERFACE = receiverInterface;
        RECEIVER_META_DATA = receiverMetaData;
        ROOT_NODE_TAG = rootNodeTag;
        ADDON_NODE_TAG = addonNodeTag;
        mBuildInAddOnsResId = buildInAddonResId;
        mReadExternalPacksToo = readExternalPacksToo;

        mActiveInstances.add(this);
    }

    protected boolean isEventRequiresCacheRefresh(Intent eventIntent, Context context) throws NameNotFoundException {
        String action = eventIntent.getAction();
        String packageNameSchemePart = eventIntent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            //will reset only if the new package has my addons
            boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
            if (hasAddon) {
                Logger.d(TAG, "It seems that an addon exists in a newly installed package " + packageNameSchemePart + ". I need to reload stuff.");
                return true;
            }
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action) || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            //If I'm managing OR it contains an addon (could be new feature in the package), I want to reset.
            boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
            if (isPackagedManaged) {
                Logger.d(TAG, "It seems that an addon I use (in package " + packageNameSchemePart + ") has been changed. I need to reload stuff.");
                return true;
            } else {
                boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
                if (hasAddon) {
                    Logger.d(TAG, "It seems that an addon exists in an updated package " + packageNameSchemePart + ". I need to reload stuff.");
                    return true;
                }
            }
        } else //removed
        {
            //so only if I manage this package, I want to reset
            boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
            if (isPackagedManaged) {
                Logger.d(TAG, "It seems that an addon I use (in package " + packageNameSchemePart + ") has been removed. I need to reload stuff.");
                return true;
            }
        }
        return false;
    }

    protected boolean isPackageManaged(String packageNameSchemePart) {
        for (AddOn addOn : mAddOns) {
            if (addOn.getPackageName().equals(packageNameSchemePart)) {
                return true;
            }
        }

        return false;
    }

    protected boolean isPackageContainAnAddon(Context context, String packageNameSchemePart) throws NameNotFoundException {
        PackageInfo newPackage = context.getPackageManager().getPackageInfo(packageNameSchemePart, PackageManager.GET_RECEIVERS + PackageManager.GET_META_DATA);
        if (newPackage.receivers != null) {
            ActivityInfo[] receivers = newPackage.receivers;
            for (ActivityInfo aReceiver : receivers) {
                //issue 904
                if (aReceiver == null || aReceiver.applicationInfo == null || !aReceiver.enabled || !aReceiver.applicationInfo.enabled)
                    continue;
                final XmlPullParser xml = aReceiver.loadXmlMetaData(context.getPackageManager(), RECEIVER_META_DATA);
                if (xml != null) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isEventRequiresViewReset(Intent eventIntent, Context context) {
        return false;
    }

    protected synchronized void clearAddOnList() {
        mAddOns.clear();
        mAddOnsById.clear();
    }

    public synchronized E getAddOnById(String id, Context askContext) {
        if (mAddOnsById.size() == 0) {
            loadAddOns(askContext);
        }
        return mAddOnsById.get(id);
    }

    public synchronized final List<E> getAllAddOns(Context askContext) {
        Logger.d(TAG, "getAllAddOns has %d add on for %s", mAddOns.size(), getClass().getName());
        if (mAddOns.size() == 0) {
            loadAddOns(askContext);
        }
        Logger.d(TAG, "getAllAddOns will return %d add on for %s", mAddOns.size(), getClass().getName());
        return Collections.unmodifiableList(mAddOns);
    }

    protected void loadAddOns(final Context askContext) {
        clearAddOnList();

        ArrayList<E> local = getAddOnsFromResId(askContext, askContext, mBuildInAddOnsResId);
        for (E addon : local) {
            Logger.d(TAG, "Local add-on %s loaded", addon.getId());
        }
        mAddOns.addAll(local);
        ArrayList<E> external = getExternalAddOns(askContext);
        for (E addon : external) {
            Logger.d(TAG, "External add-on %s loaded", addon.getId());
        }
        mAddOns.addAll(external);
        Logger.d(TAG, "Have %d add on for %s", mAddOns.size(), getClass().getName());

        buildOtherDataBasedOnNewAddOns(mAddOns);

        //sorting the keyboards according to the requested
        //sort order (from minimum to maximum)
        Collections.sort(mAddOns, new AddOnsComparator(askContext));
        Logger.d(TAG, "Have %d add on for %s (after sort)", mAddOns.size(), getClass().getName());
    }

    protected void buildOtherDataBasedOnNewAddOns(ArrayList<E> newAddOns) {
        for (E addOn : newAddOns)
            mAddOnsById.put(addOn.getId(), addOn);
    }

    private ArrayList<E> getExternalAddOns(Context askContext) {
        final ArrayList<E> externalAddOns = new ArrayList<>();

        if (!mReadExternalPacksToo)//this will disable external packs (API careful stage)
            return externalAddOns;

        final List<ResolveInfo> broadcastReceivers =
                askContext.getPackageManager().queryBroadcastReceivers(new Intent(RECEIVER_INTERFACE), PackageManager.GET_META_DATA);


        for (final ResolveInfo receiver : broadcastReceivers) {
            if (receiver.activityInfo == null) {
                Logger.e(TAG, "BroadcastReceiver has null ActivityInfo. Receiver's label is "
                        + receiver.loadLabel(askContext.getPackageManager()));
                Logger.e(TAG, "Is the external keyboard a service instead of BroadcastReceiver?");
                // Skip to next receiver
                continue;
            }

            if (!receiver.activityInfo.enabled || !receiver.activityInfo.applicationInfo.enabled) continue;

            try {
                final Context externalPackageContext = askContext.createPackageContext(receiver.activityInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
                final ArrayList<E> packageAddOns = getAddOnsFromActivityInfo(askContext, externalPackageContext, receiver.activityInfo);

                externalAddOns.addAll(packageAddOns);
            } catch (final NameNotFoundException e) {
                Logger.e(TAG, "Did not find package: " + receiver.activityInfo.packageName);
            }

        }

        return externalAddOns;
    }

    private ArrayList<E> getAddOnsFromResId(Context askContext, Context context, int addOnsResId) {
        final XmlPullParser xml = context.getResources().getXml(addOnsResId);
        if (xml == null)
            return new ArrayList<>();
        return parseAddOnsFromXml(askContext, context, xml);
    }

    private ArrayList<E> getAddOnsFromActivityInfo(Context askContext, Context context, ActivityInfo ai) {
        final XmlPullParser xml = ai.loadXmlMetaData(context.getPackageManager(), RECEIVER_META_DATA);
        if (xml == null)//issue 718: maybe a bad package?
            return new ArrayList<>();
        return parseAddOnsFromXml(askContext, context, xml);
    }

    private ArrayList<E> parseAddOnsFromXml(Context askContext, Context context, XmlPullParser xml) {
        final ArrayList<E> addOns = new ArrayList<>();
        try {
            int event;
            boolean inRoot = false;
            while ((event = xml.next()) != XmlPullParser.END_DOCUMENT) {
                final String tag = xml.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (ROOT_NODE_TAG.equals(tag)) {
                        inRoot = true;
                    } else if (inRoot && ADDON_NODE_TAG.equals(tag)) {
                        final AttributeSet attrs = Xml.asAttributeSet(xml);
                        E addOn = createAddOnFromXmlAttributes(askContext, attrs, context);
                        if (addOn != null) {
                            addOns.add(addOn);
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (ROOT_NODE_TAG.equals(tag)) {
                        inRoot = false;
                        break;
                    }
                }
            }
        } catch (final IOException e) {
            Logger.e(TAG, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Logger.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

        return addOns;
    }

    private E createAddOnFromXmlAttributes(Context askContext, AttributeSet attrs, Context context) {
        String prefId = "UNKNOWN";
        int nameId = AddOn.INVALID_RES_ID;
        try {
            prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
            nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
            final int descriptionInt = attrs.getAttributeResourceValue(null, XML_DESCRIPTION_ATTRIBUTE, AddOn.INVALID_RES_ID);
            //NOTE, to be compatible we need this. because the most of descriptions are
            //without @string/adb
            String description;
            if (descriptionInt != AddOn.INVALID_RES_ID) {
                description = context.getResources().getString(descriptionInt);
            } else {
                description = attrs.getAttributeValue(null, XML_DESCRIPTION_ATTRIBUTE);
            }

            final int sortIndex = attrs.getAttributeUnsignedIntValue(null, XML_SORT_INDEX_ATTRIBUTE, 1);

            // asserting
            if (TextUtils.isEmpty(prefId) || (nameId == AddOn.INVALID_RES_ID)) {
                Logger.e(TAG, "External add-on does not include all mandatory details! Will not create add-on. prefId %s, nameId %d", prefId, nameId);
                return null;
            } else {
                Logger.d(TAG, "External addon details with prefId %s, nameId %d", prefId, nameId);
                return createConcreteAddOn(askContext, context, prefId, nameId, description, sortIndex, attrs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.w(TAG, e, "Failed to load add-on id %s, name-res-id %d", prefId, nameId);
            return null;
        }
    }

    protected abstract E createConcreteAddOn(Context askContext, Context context, String prefId, int nameId, String description, int sortIndex, AttributeSet attrs);
}
