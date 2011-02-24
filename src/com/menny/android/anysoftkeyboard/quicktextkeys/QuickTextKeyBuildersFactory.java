package com.menny.android.anysoftkeyboard.quicktextkeys;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Malcolm
 */
public class QuickTextKeyBuildersFactory {
	private static final String TAG = "ASK QTK creator factory";

	public interface QuickTextKeyBuilder {

		/**
		 * This is the interface name that a broadcast receiver implementing an
		 * external quick text key should say that it supports -- that is, this is the
		 * action it uses for its intent filter.
		 */
		public static final String RECEIVER_INTERFACE = "com.anysoftkeyboard.plugin.QUICK_TEXT_KEY";
		/**
		 * Name under which an external quick text key broadcast receiver component
		 * publishes information about itself.
		 */
		public static final String RECEIVER_META_DATA = "com.anysoftkeyboard.plugindata.quicktextkeys";

		QuickTextKey createQuickTextKey();
		String getId();
		int getQuickTextKeyIndex();
		int getQuickTextKeyNameResId();
		String getDescription();
		Context getPackageContext();
	}

	public static class QuickTextKeyBuilderImpl implements QuickTextKeyBuilder {
		private final String mId;
		private final int mNameResId;
		private final int mPopupKeyboardResId;
		private final int mPopupListNamesResId;
		private final int mPopupListValuesResId;
		private final int mKeyIconResId;
		private final int mKeyLabelResId;
		private final int mKeyOutputTextResId;
		private final int mIconPreviewResId;
		private final String mDescription;
		private final int mIndex;
		private final Context mPackageContext;

		public QuickTextKeyBuilderImpl(Context packageContext, String id, int nameResId,
				int popupKeyboardResId,	int popupListNamesResId, int popupListValuesResId,
				int keyIconResId, int keyLabelResId, int keyOutputTextResId, int iconPreviewResId,
				String description,	int index) {
			this.mPackageContext = packageContext;
			this.mId = id;
			this.mNameResId = nameResId;
			this.mPopupKeyboardResId = popupKeyboardResId;
			this.mPopupListNamesResId = popupListNamesResId;
			this.mPopupListValuesResId = popupListValuesResId;
			this.mKeyIconResId = keyIconResId;
			this.mKeyLabelResId = keyLabelResId;
			this.mKeyOutputTextResId = keyOutputTextResId;
			this.mIconPreviewResId = iconPreviewResId;
			this.mDescription = description;
			this.mIndex = index;

			if (AnySoftKeyboardConfiguration.DEBUG) {
				Log.d("ASK " + QuickTextKeyBuilderImpl.class.getSimpleName(),
						"Creator for " + mId + " package:"
						+ mPackageContext.getPackageName() + " popupKBD: " + mPopupKeyboardResId
						+ " popupList names: " + mPopupListNamesResId + " popupList values: "
						+ mPopupListValuesResId);
			}
		}

		public QuickTextKey createQuickTextKey() {
			if (AnySoftKeyboardConfiguration.DEBUG) {
				Log.d(TAG, "Creating external quick text key '" + mId);
			}
			return new QuickTextKey(mPackageContext, mId, mNameResId, mPopupKeyboardResId,
					mPopupListNamesResId, mPopupListValuesResId, mKeyIconResId, mKeyLabelResId,
					mKeyOutputTextResId, mIconPreviewResId,	mDescription);
		}

		public String getId() {
			return mId;
		}

		public int getQuickTextKeyIndex() {
			return mIndex;
		}

		public int getQuickTextKeyNameResId() {
			return mNameResId;
		}

		public String getDescription() {
			return mDescription;
		}

		public Context getPackageContext() {
			return mPackageContext;
		}
	}

	private static ArrayList<QuickTextKeyBuilder> keyBuilders = null;
	private static final String XML_QUICK_TEXT_KEYS_TAG = "QuickTextKeys";
	private static final String XML_QUICK_TEXT_KEY_TAG = "QuickTextKey";
	private static final String XML_PREF_ID_ATTRIBUTE = "id";
	private static final String XML_NAME_RES_ID_ATTRIBUTE = "nameResId";
	private static final String XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE = "popupKeyboard";
	private static final String XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE = "popupListText";
	private static final String XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE = "popupListOutput";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "keyIcon";
	private static final String XML_KEY_LABEL_RES_ID_ATTRIBUTE = "keyLabel";
	private static final String XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE = "keyOutputText";
	private static final String XML_ICON_PREVIEW_RES_ID_ATTRIBUTE = "iconPreview";
	private static final String XML_DESCRIPTION_ATTRIBUTE = "description";
	private static final String XML_INDEX_ATTRIBUTE = "index";

	public synchronized static void resetBuildersCache() {
		keyBuilders = null;
	}

	public synchronized static void onPackageSetupChanged(String packageName) {}

	public synchronized static ArrayList<QuickTextKeyBuilder> getAllBuilders(final Context
			context) {
		if (keyBuilders == null) { //Lazy initialization
			final ArrayList<QuickTextKeyBuilder> keys = new ArrayList<QuickTextKeyBuilder>();
			keys.addAll(getQuickTextKeyCreatorsFromResId(context, R.xml.quick_text_keys));
			keys.addAll(getAllExternalQuickTextKeyCreators(context));
			keyBuilders = keys;

			//Sorting the keys according to the requested sort order (from minimum to maximum)
			Collections.sort(keyBuilders, new Comparator<QuickTextKeyBuilder>() {
				public int compare(QuickTextKeyBuilder k1, QuickTextKeyBuilder k2) {
					Context c1 = k1.getPackageContext();
					Context c2 = k2.getPackageContext();
					if (c1 == null) {
						c1 = context;
					}
					if (c2 == null) {
						c2 = context;
					}

					if (c1 == c2) {
						return k1.getQuickTextKeyIndex() - k2.getQuickTextKeyIndex();
					} else if (c1 == context) {//I want to make sure ASK packages are first
						return -1;
					} else if (c2 == context) {
						return 1;
					} else {
						return c1.getPackageName().compareToIgnoreCase(c2.getPackageName());
					}
				}
			});
		}
		return keyBuilders;
	}

	private static ArrayList<QuickTextKeyBuilder> getAllExternalQuickTextKeyCreators(Context
			context) {

		final List<ResolveInfo> broadcastReceivers = context.getPackageManager()
				.queryBroadcastReceivers(new Intent(QuickTextKeyBuilder.RECEIVER_INTERFACE),
				PackageManager.GET_META_DATA);

		final ArrayList<QuickTextKeyBuilder> externalKeyCreators =
				new ArrayList<QuickTextKeyBuilder>();
		for (final ResolveInfo receiver : broadcastReceivers) {
			// If activityInfo is null, we are probably dealing with a service.
			if (receiver.activityInfo == null) {
				Log.e(TAG, "BroadcastReceiver has null ActivityInfo. Receiver's label is "
						+ receiver.loadLabel(context.getPackageManager()));
				Log.e(TAG, "Is the external key a service instead of BroadcastReceiver?");
				// Skip to next receiver
				continue;
			}

			try {
				final Context externalPackageContext = context.createPackageContext(
						receiver.activityInfo.packageName, PackageManager.GET_META_DATA);
				final ArrayList<QuickTextKeyBuilder> packageKeyCreators =
						getQuickTextKeyCreatorsFromActivityInfo(externalPackageContext,
						receiver.activityInfo);
				externalKeyCreators.addAll(packageKeyCreators);
			} catch (final NameNotFoundException e) {
				Log.e(TAG, "Did not find package: " + receiver.activityInfo.packageName);
			}
		}

		return externalKeyCreators;
	}

	private static ArrayList<QuickTextKeyBuilder> getQuickTextKeyCreatorsFromResId(Context context,
			int keyboardsResId) {
		final XmlPullParser allKeyboards = context.getResources().getXml(keyboardsResId);
		return parseKeyboardCreatorsFromXml(context, allKeyboards);
	}

	private static ArrayList<QuickTextKeyBuilder> getQuickTextKeyCreatorsFromActivityInfo(
			Context context, ActivityInfo ai) {
		final XmlPullParser allKeyboards = ai.loadXmlMetaData(context.getPackageManager(),
				QuickTextKeyBuilder.RECEIVER_META_DATA);
		return parseKeyboardCreatorsFromXml(context, allKeyboards);
	}

	private static ArrayList<QuickTextKeyBuilder> parseKeyboardCreatorsFromXml(Context context,
			XmlPullParser allKeysParser) {
		final ArrayList<QuickTextKeyBuilder> keyboards = new ArrayList<QuickTextKeyBuilder>();
		try {
			boolean insideQuickTextKeys = false;
			while (true) {
				int event = allKeysParser.next();
				if (event == XmlPullParser.END_DOCUMENT) break;

				final String tag = allKeysParser.getName();
				if (event == XmlPullParser.START_TAG) {
					if (XML_QUICK_TEXT_KEYS_TAG.equals(tag)) {
						insideQuickTextKeys = true;
					} else if (insideQuickTextKeys && XML_QUICK_TEXT_KEY_TAG.equals(tag)) {
						final AttributeSet attrs = Xml.asAttributeSet(allKeysParser);

						final String prefId = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
						final int nameId = attrs.getAttributeResourceValue(null,
								XML_NAME_RES_ID_ATTRIBUTE, -1);
						final int popupKeyboardResId = attrs.getAttributeResourceValue(null,
								XML_POPUP_KEYBOARD_RES_ID_ATTRIBUTE, -1);
						final int popupListTextResId = attrs.getAttributeResourceValue(null,
								XML_POPUP_LIST_TEXT_RES_ID_ATTRIBUTE, -1);
						final int popupListOutputResId = attrs.getAttributeResourceValue(null,
								XML_POPUP_LIST_OUTPUT_RES_ID_ATTRIBUTE, -1);
						final int iconResId = attrs.getAttributeResourceValue(null,
								XML_ICON_RES_ID_ATTRIBUTE, -1); //Maybe should make a default icon
						final int keyLabelResId = attrs.getAttributeResourceValue(null,
								XML_KEY_LABEL_RES_ID_ATTRIBUTE, -1);
						final int keyOutputTextResId = attrs.getAttributeResourceValue(null,
								XML_KEY_OUTPUT_TEXT_RES_ID_ATTRIBUTE, -1);
						final int keyIconPreviewResId = attrs.getAttributeResourceValue(null,
								XML_ICON_PREVIEW_RES_ID_ATTRIBUTE, -1);
						final int descriptionInt = attrs.getAttributeResourceValue(null,
								XML_DESCRIPTION_ATTRIBUTE, -1);
						/* The next parameter can not only be a pointer to a resource, but also
						 * a plain string, so we should check what case we are handling.
						 */
						String description;
						if (descriptionInt != -1) {
							description = context.getResources().getString(descriptionInt);
						} else {
							description = attrs.getAttributeValue(null,
									XML_DESCRIPTION_ATTRIBUTE);
						}

						final int keyIndex = attrs.getAttributeUnsignedIntValue(null,
								XML_INDEX_ATTRIBUTE, 1);

						/* We need prefId, keyLabelResId, and nameId to be set, also we need either
						 * popupKeyboardResId or both popupListTextResId and
						 * popupListTextResId.
						 */
						if ((prefId == null) || (nameId == -1) || (keyLabelResId == -1) ||
								((popupKeyboardResId == -1) &&
								(popupListTextResId == -1 || popupListTextResId == -1))) {
							Log.e(TAG, "External quick text key does not include all the mandatory"
									+ " details! The key won't be created");
						} else {
							if (AnySoftKeyboardConfiguration.DEBUG) {
								Log.d(TAG,
										"External key details: prefId:" + prefId + " nameId:"
										+ nameId + " popupKeyboardResId:" + popupKeyboardResId
										+ " popupListText:" + popupListTextResId
										+ " popupListOutput:" + popupListOutputResId);
							}
							final QuickTextKeyBuilder creator = new QuickTextKeyBuilderImpl(
									context, prefId, nameId, popupKeyboardResId, popupListTextResId,
									popupListOutputResId, iconResId, keyLabelResId,
									keyOutputTextResId,	keyIconPreviewResId, description, keyIndex);

							keyboards.add(creator);
						}

					}
				} else if (event == XmlPullParser.END_TAG) {
					if (XML_QUICK_TEXT_KEYS_TAG.equals(tag)) {
						insideQuickTextKeys = false;
						break;
					}
				}
			}
		} catch (final IOException e) {
			Log.e(TAG, "IO error:" + e);
		} catch (final XmlPullParserException e) {
			Log.e(TAG, "Parsing error:" + e);
		}

		return keyboards;
	}
}