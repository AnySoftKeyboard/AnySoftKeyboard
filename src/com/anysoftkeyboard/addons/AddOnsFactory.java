package com.anysoftkeyboard.addons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

public abstract class AddOnsFactory<E extends AddOn> {

	private static final class AddOnsComparator implements Comparator<AddOn> {
		private final Context askContext;

		private AddOnsComparator(Context askContext) {
			this.askContext = askContext;
		}

		public int compare(AddOn k1, AddOn k2)
		{
			Context c1 = k1.getPackageContext();
			Context c2 = k2.getPackageContext();
			if (c1 == null)
				c1 = askContext;
			if (c2 == null)
				c2 = askContext;

			if (c1 == c2)
				return k1.getSortIndex() - k2.getSortIndex();
			else if (c1 == askContext)//I want to make sure ASK packages are first
				return -1;
			else if (c2 == askContext)
				return 1;
			else
				return c1.getPackageName().compareToIgnoreCase(c2.getPackageName());
		}
	}

	private final static ArrayList<AddOnsFactory<?> > mActiveInstances = new  ArrayList<AddOnsFactory<?> >();

	private static final String sTAG = "AddOnsFactory";

	public static void onPackageChanged(final Intent eventIntent, final AnySoftKeyboard ask)
	{
		boolean cleared = false;
		boolean recreateView = false;
		for(AddOnsFactory<?> factory : mActiveInstances)
		{
			try {
				if (factory.isEventRequiresCacheRefresh(eventIntent, ask.getApplicationContext())) {
					cleared = true;
					if (factory.isEventRequiresViewReset(eventIntent, ask.getApplicationContext())) recreateView = true;
					Log.d(sTAG, factory.getClass().getName()+" will handle this package-changed event. Also recreate view? "+recreateView);
					factory.clearAddOnList();
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (cleared) ask.resetKeyboardView(recreateView);
	}

	public static AddOn locateAddOn(String id, Context askContext) {
		for(AddOnsFactory<?> factory : mActiveInstances) {
			AddOn addOn = factory.getAddOnById(id, askContext);
			if (addOn != null) {
				if (AnyApplication.DEBUG) Log.d(sTAG, "Located addon with id "+addOn.getId()+" of type "+addOn.getClass().getName());
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

    private final ArrayList<E> mAddOns = new ArrayList<E>();
    private final HashMap<String, E> mAddOnsById = new HashMap<String, E>();
    
    private final boolean mReadExternalPacksToo;
    private final String ROOT_NODE_TAG;
    private final String ADDON_NODE_TAG;
    private final int mBuildInAddOnsResId;
    
    private static final String XML_PREF_ID_ATTRIBUTE = "id";
    private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
    private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
    private static final String XML_SORT_INDEX_ATTRIBUTE = "index";
    
    protected AddOnsFactory(String tag, String receiverInterface, String receiverMetaData, String rootNodeTag, String addonNodeTag, int buildInAddonResId, boolean readExternalPacksToo)
    {
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
		if (Intent.ACTION_PACKAGE_ADDED.equals(action))
		{
			//will reset only if the new package has my addons
			boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
			if (hasAddon)
			{
				Log.d(TAG, "It seems that an addon exists in a newly installed package "+packageNameSchemePart+". I need to reload stuff.");
				return true;
			}
		}
		else if (Intent.ACTION_PACKAGE_REPLACED.equals(action) || Intent.ACTION_PACKAGE_CHANGED.equals(action))
		{
			//If I'm managing OR it contains an addon (could be new feature in the package), I want to reset.
			boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
			if (isPackagedManaged)
			{
				Log.d(TAG, "It seems that an addon I use (in package "+packageNameSchemePart+") has been changed. I need to reload stuff.");
				return true;
			}
			else
			{
				boolean hasAddon = isPackageContainAnAddon(context, packageNameSchemePart);
				if (hasAddon)
				{
					Log.d(TAG, "It seems that an addon exists in an updated package "+packageNameSchemePart+". I need to reload stuff.");
					return true;
				}
			}
		}
		else //removed
		{
			//so only if I manage this package, I want to reset
			boolean isPackagedManaged = isPackageManaged(packageNameSchemePart);
			if (isPackagedManaged)
			{
				Log.d(TAG, "It seems that an addon I use (in package "+packageNameSchemePart+") has been removed. I need to reload stuff.");
				return true;
			}
		}
		return false;
	}
    	
    protected boolean isPackageManaged(String packageNameSchemePart) {
    	for (AddOn addOn : mAddOns) {
			if (addOn.getPackageContext().getPackageName().equals(packageNameSchemePart)) {
				return true;
			}
		}
    	
    	return false;
	}

    protected boolean isPackageContainAnAddon(Context context, String packageNameSchemePart) throws NameNotFoundException {
    	PackageInfo newPackage = context.getPackageManager().getPackageInfo(packageNameSchemePart, PackageManager.GET_RECEIVERS+PackageManager.GET_META_DATA);
    	if (newPackage.receivers != null)
		{
			ActivityInfo[] receivers = newPackage.receivers;
			for(ActivityInfo aReceiver : receivers)
			{
				//issue 904
				if (aReceiver == null || aReceiver.applicationInfo == null || !aReceiver.enabled || !aReceiver.applicationInfo.enabled) continue;
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
        
    public synchronized E getAddOnById(String id, Context askContext)
    {
    	if (mAddOnsById.size() == 0) {
        	loadAddOns(askContext);
        }
    	return mAddOnsById.get(id);
    }
    
	public synchronized final ArrayList<E> getAllAddOns(Context askContext) {

        if (mAddOns.size() == 0) {
        	loadAddOns(askContext);
        }
        return mAddOns;
    }

	protected void loadAddOns(final Context askContext) {
		clearAddOnList();
		
		mAddOns.addAll(getAddOnsFromResId(askContext, askContext, mBuildInAddOnsResId));
		mAddOns.addAll(getExternalAddOns(askContext));
		
		buildOtherDataBasedOnNewAddOns(mAddOns);
		
		//sorting the keyboards according to the requested
		//sort order (from minimum to maximum)
		Collections.sort(mAddOns, new AddOnsComparator(askContext));
	}
	
	protected void buildOtherDataBasedOnNewAddOns(ArrayList<E> newAddOns) {
		for(E addOn : newAddOns)
			mAddOnsById.put(addOn.getId(), addOn);
	}

	private ArrayList<E> getExternalAddOns(Context askContext){
		final ArrayList<E> externalAddOns = new ArrayList<E>();
		
		if (!mReadExternalPacksToo)//this will disable external packs (API careful stage)
			return externalAddOns;
		
        final List<ResolveInfo> broadcastReceivers = 
        		askContext.getPackageManager().queryBroadcastReceivers(new Intent(RECEIVER_INTERFACE), PackageManager.GET_META_DATA);

        
        for(final ResolveInfo receiver : broadcastReceivers){
            if (receiver.activityInfo == null) {
                Log.e(TAG, "BroadcastReceiver has null ActivityInfo. Receiver's label is "
                        + receiver.loadLabel(askContext.getPackageManager()));
                Log.e(TAG, "Is the external keyboard a service instead of BroadcastReceiver?");
                // Skip to next receiver
                continue;
            }

            if (!receiver.activityInfo.enabled || !receiver.activityInfo.applicationInfo.enabled) continue;

            try {
                final Context externalPackageContext = askContext.createPackageContext(receiver.activityInfo.packageName, PackageManager.GET_META_DATA);
                final ArrayList<E> packageAddOns = getAddOnsFromActivityInfo(askContext, externalPackageContext, receiver.activityInfo);
                
                externalAddOns.addAll(packageAddOns);
            } catch (final NameNotFoundException e) {
                Log.e(TAG, "Did not find package: " + receiver.activityInfo.packageName);
            }

        }

        return externalAddOns;
    }

    private ArrayList<E> getAddOnsFromResId(Context askContext, Context context, int addOnsResId) {
        final XmlPullParser xml = context.getResources().getXml(addOnsResId);
        if (xml == null)
        	return new ArrayList<E>();
        return parseAddOnsFromXml(askContext, context, xml);
    }

    private ArrayList<E> getAddOnsFromActivityInfo(Context askContext, Context context, ActivityInfo ai) {
        final XmlPullParser xml = ai.loadXmlMetaData(context.getPackageManager(), RECEIVER_META_DATA);
        if (xml == null)//issue 718: maybe a bad package?
        	return new ArrayList<E>();
        return parseAddOnsFromXml(askContext, context, xml);
    }

    private ArrayList<E> parseAddOnsFromXml(Context askContext, Context context, XmlPullParser xml) {
        final ArrayList<E> addOns = new ArrayList<E>();
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
                    	if (addOn != null)
                    	{
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
            Log.e(TAG, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

        return addOns;
    }

	private E createAddOnFromXmlAttributes(Context askContext, AttributeSet attrs, Context context) {
        final String prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
        final int nameId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, AddOn.INVALID_RES_ID);
        final int descriptionInt = attrs.getAttributeResourceValue(null, XML_DESCRIPTION_ATTRIBUTE, AddOn.INVALID_RES_ID);
        //NOTE, to be compatibel we need this. because the most of descriptions are
        //without @string/adb
        String description;
        if(descriptionInt != AddOn.INVALID_RES_ID){
            description = context.getResources().getString(descriptionInt);
        } else {
            description =  attrs.getAttributeValue(null, XML_DESCRIPTION_ATTRIBUTE); 
        }

        final int sortIndex = attrs.getAttributeUnsignedIntValue(null, XML_SORT_INDEX_ATTRIBUTE, 1);
        
        // asserting
        if ((prefId == null) || (nameId == AddOn.INVALID_RES_ID)) {
            Log.e(TAG, "External add-on does not include all mandatory details! Will not create add-on.");
            return null;
        } else {
            if (AnyApplication.DEBUG) {
                Log.d(TAG, "External addon details: prefId:" + prefId + " nameId:" + nameId);
            }
            return createConcreateAddOn(askContext, context, prefId, nameId, description, sortIndex, attrs);
        }
	}

	protected abstract E createConcreateAddOn(Context askContext, Context context, String prefId, int nameId,
			String description, int sortIndex, AttributeSet attrs);
}
