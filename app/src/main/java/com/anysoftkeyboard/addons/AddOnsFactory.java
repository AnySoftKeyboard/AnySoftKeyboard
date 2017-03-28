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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Xml;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.BuildConfig;

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

    private static final ArrayList<AddOnsFactory<?>> msActiveInstances = new ArrayList<>();

    private static final String sTAG = "AddOnsFactory";

    public static void onPackageChanged(final Intent eventIntent, final AnySoftKeyboard ask) {
        boolean cleared = false;
        boolean recreateView = false;
        for (AddOnsFactory<?> factory : msActiveInstances) {
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
        for (AddOnsFactory<?> factory : msActiveInstances) {
            AddOn addOn = factory.getAddOnById(id, askContext);
            if (addOn != null) {
                Logger.d(sTAG, "Located addon with id " + addOn.getId() + " of type " + addOn.getClass().getName());
                return addOn;
            }
        }

        return null;
    }

    protected final String mTag;

    /**
     * This is the interface name that a broadcast receiver implementing an
     * external addon should say that it supports -- that is, this is the
     * action it uses for its intent filter.
     */
    private final String mReceiverInterface;

    /**
     * Name under which an external addon broadcast receiver component
     * publishes information about itself.
     */
    private final String mReceiverMetaData;

    private final ArrayList<E> mAddOns = new ArrayList<>();
    private final HashMap<String, E> mAddOnsById = new HashMap<>();

    private final boolean mReadExternalPacksToo;
    private final String mRootNodeTag;
    private final String mAddonNodeTag;
    private final int mBuildInAddOnsResId;
    private final boolean mDevAddOnsIncluded;

    private static final String XML_PREF_ID_ATTRIBUTE = "id";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
    private static final String XML_SORT_INDEX_ATTRIBUTE = "index";
    private static final String XML_DEV_ADD_ON_ATTRIBUTE = "devOnly";
    private static final String XML_HIDDEN_ADD_ON_ATTRIBUTE = "hidden";

    protected AddOnsFactory(String tag, String receiverInterface, String receiverMetaData, String rootNodeTag, String addonNodeTag, int buildInAddonResId, boolean readExternalPacksToo) {
        this(tag, receiverInterface, receiverMetaData, rootNodeTag, addonNodeTag, buildInAddonResId, readExternalPacksToo, BuildConfig.TESTING_BUILD);
    }

    protected AddOnsFactory(String tag, String receiverInterface, String receiverMetaData, String rootNodeTag, String addonNodeTag, int buildInAddonResId, boolean readExternalPacksToo, boolean isDebugBuild) {
        mTag = tag;
        this.mReceiverInterface = receiverInterface;
        this.mReceiverMetaData = receiverMetaData;
        this.mRootNodeTag = rootNodeTag;
        this.mAddonNodeTag = addonNodeTag;
        mBuildInAddOnsResId = buildInAddonResId;
        mReadExternalPacksToo = readExternalPacksToo;
        mDevAddOnsIncluded = isDebugBuild;

        msActiveInstances.add(this);
    }

    private boolean isEventRequiresCacheRefresh(Intent eventIntent, Context context) throws NameNotFoundException {
        String action = eventIntent.getAction();
        String packageNameSchemePart = eventIntent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            //will reset only if the new package has my addons
            boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
            if (hasAddon) {
                Logger.d(mTag, "It seems that an addon exists in a newly installed package " + packageNameSchemePart + ". I need to reload stuff.");
                return true;
            }
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action) || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            //If I'm managing OR it contains an addon (could be new feature in the package), I want to reset.
            boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
            if (isPackagedManaged) {
                Logger.d(mTag, "It seems that an addon I use (in package " + packageNameSchemePart + ") has been changed. I need to reload stuff.");
                return true;
            } else {
                boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
                if (hasAddon) {
                    Logger.d(mTag, "It seems that an addon exists in an updated package " + packageNameSchemePart + ". I need to reload stuff.");
                    return true;
                }
            }
        } else //removed
        {
            //so only if I manage this package, I want to reset
            boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
            if (isPackagedManaged) {
                Logger.d(mTag, "It seems that an addon I use (in package " + packageNameSchemePart + ") has been removed. I need to reload stuff.");
                return true;
            }
        }
        return false;
    }

    private boolean isPackageManaged(String packageNameSchemePart) {
        for (AddOn addOn : mAddOns) {
            if (addOn.getPackageName().equals(packageNameSchemePart)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPackageContainAnAddon(Context context, String packageNameSchemePart) throws NameNotFoundException {
        PackageInfo newPackage = context.getPackageManager().getPackageInfo(packageNameSchemePart, PackageManager.GET_RECEIVERS + PackageManager.GET_META_DATA);
        if (newPackage.receivers != null) {
            ActivityInfo[] receivers = newPackage.receivers;
            for (ActivityInfo aReceiver : receivers) {
                //issue 904
                if (aReceiver == null || aReceiver.applicationInfo == null || !aReceiver.enabled || !aReceiver.applicationInfo.enabled)
                    continue;
                final XmlPullParser xml = aReceiver.loadXmlMetaData(context.getPackageManager(), mReceiverMetaData);
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

    protected synchronized E getAddOnById(String id, Context askContext) {
        if (mAddOnsById.size() == 0) {
            loadAddOns(askContext);
        }
        return mAddOnsById.get(id);
    }

    protected final synchronized List<E> getAllAddOns(Context askContext) {
        Logger.d(mTag, "getAllAddOns has %d add on for %s", mAddOns.size(), getClass().getName());
        if (mAddOns.size() == 0) {
            loadAddOns(askContext);
        }
        Logger.d(mTag, "getAllAddOns will return %d add on for %s", mAddOns.size(), getClass().getName());
        return Collections.unmodifiableList(mAddOns);
    }

    protected void loadAddOns(final Context askContext) {
        clearAddOnList();

        List<E> local = getAddOnsFromResId(askContext, askContext, mBuildInAddOnsResId);
        for (E addon : local) {
            Logger.d(mTag, "Local add-on %s loaded", addon.getId());
        }
        mAddOns.addAll(local);
        List<E> external = getExternalAddOns(askContext);
        for (E addon : external) {
            Logger.d(mTag, "External add-on %s loaded", addon.getId());
        }
        mAddOns.addAll(external);
        Logger.d(mTag, "Have %d add on for %s", mAddOns.size(), getClass().getName());

        buildOtherDataBasedOnNewAddOns(mAddOns);

        //sorting the keyboards according to the requested
        //sort order (from minimum to maximum)
        Collections.sort(mAddOns, new AddOnsComparator(askContext));
        Logger.d(mTag, "Have %d add on for %s (after sort)", mAddOns.size(), getClass().getName());
    }

    protected void buildOtherDataBasedOnNewAddOns(ArrayList<E> newAddOns) {
        for (E addOn : newAddOns)
            mAddOnsById.put(addOn.getId(), addOn);
        //removing hidden addons from global list, so hidden addons exist only in the mapping
        for (E addOn : mAddOnsById.values()) {
            if (addOn instanceof AddOnImpl && ((AddOnImpl)addOn).isHiddenAddon()) {
                newAddOns.remove(addOn);
            }
        }
    }

    private List<E> getExternalAddOns(Context askContext) {
        if (!mReadExternalPacksToo)//this will disable external packs (API careful stage)
            return Collections.emptyList();

        final List<ResolveInfo> broadcastReceivers =
                askContext.getPackageManager().queryBroadcastReceivers(new Intent(mReceiverInterface), PackageManager.GET_META_DATA);

        final List<E> externalAddOns = new ArrayList<>();

        for (final ResolveInfo receiver : broadcastReceivers) {
            if (receiver.activityInfo == null) {
                Logger.e(mTag, "BroadcastReceiver has null ActivityInfo. Receiver's label is "
                        + receiver.loadLabel(askContext.getPackageManager()));
                Logger.e(mTag, "Is the external keyboard a service instead of BroadcastReceiver?");
                // Skip to next receiver
                continue;
            }

            if (!receiver.activityInfo.enabled || !receiver.activityInfo.applicationInfo.enabled) continue;

            try {
                final Context externalPackageContext = askContext.createPackageContext(receiver.activityInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
                final List<E> packageAddOns = getAddOnsFromActivityInfo(askContext, externalPackageContext, receiver.activityInfo);

                externalAddOns.addAll(packageAddOns);
            } catch (final NameNotFoundException e) {
                Logger.e(mTag, "Did not find package: " + receiver.activityInfo.packageName);
            }

        }

        return externalAddOns;
    }

    private List<E> getAddOnsFromResId(Context askContext, Context context, int addOnsResId) {
        final XmlPullParser xml = context.getResources().getXml(addOnsResId);
        if (xml == null)
            return Collections.emptyList();
        return parseAddOnsFromXml(askContext, context, xml);
    }

    private List<E> getAddOnsFromActivityInfo(Context askContext, Context context, ActivityInfo ai) {
        final XmlPullParser xml = ai.loadXmlMetaData(context.getPackageManager(), mReceiverMetaData);
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
                    if (mRootNodeTag.equals(tag)) {
                        inRoot = true;
                    } else if (inRoot && mAddonNodeTag.equals(tag)) {
                        final AttributeSet attrs = Xml.asAttributeSet(xml);
                        E addOn = createAddOnFromXmlAttributes(askContext, attrs, context);
                        if (addOn != null) {
                            addOns.add(addOn);
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (mRootNodeTag.equals(tag)) {
                        inRoot = false;
                        break;
                    }
                }
            }
        } catch (final IOException e) {
            Logger.e(mTag, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Logger.e(mTag, "Parse error:" + e);
            e.printStackTrace();
        }

        return addOns;
    }

    @Nullable
    private E createAddOnFromXmlAttributes(Context askContext, AttributeSet attrs, Context context) {
        final String prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
        final int nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);

        if ((!mDevAddOnsIncluded) && attrs.getAttributeBooleanValue(null, XML_DEV_ADD_ON_ATTRIBUTE, false)) {
            Logger.w(mTag, "Discarding add-on %s (name-id %d) since it is marked as DEV addon, and we're not a TESTING_BUILD build.", prefId, nameId);
            return null;
        }

        final boolean isHidden = attrs.getAttributeBooleanValue(null, XML_HIDDEN_ADD_ON_ATTRIBUTE, false);
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
        if ((prefId == null) || (nameId == AddOn.INVALID_RES_ID)) {
            Logger.e(mTag, "External add-on does not include all mandatory details! Will not create add-on.");
            return null;
        } else {
            Logger.d(mTag, "External addon details: prefId:" + prefId + " nameId:" + nameId);
            return createConcreteAddOn(askContext, context, prefId, nameId, description, isHidden, sortIndex, attrs);
        }
    }

    protected abstract E createConcreteAddOn(Context askContext, Context context, String prefId, int nameId, String description, boolean isHidden, int sortIndex, AttributeSet attrs);
}
