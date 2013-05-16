/*
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

package com.anysoftkeyboard.dictionaries;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.util.Log;

import com.anysoftkeyboard.utils.XmlUtils;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class accesses a dictionary of corrections to frequent misspellings.
 */

public class AutoText {

	private static final String mFileDirname = "/AnySoftKeyboard/AutoText";
	private static String mLanguage;
	protected static final String TAG = "ASK AutoText";

	private static ConcurrentRadixTree<String> mTree = null;
	private static Map<String, ConcurrentRadixTree<String>> mTrees = new HashMap<String, ConcurrentRadixTree<String>>();
	private int mResId;
	private Resources mResources;
	private static ThreadGroup mThreadgroup = new ThreadGroup("");

	AutoText(Resources resources, int resId, String language) {

		mLanguage = language;
		mResId = resId;
		mResources = resources;

		new Thread(mThreadgroup, new Runnable() {

			@Override
			public void run() {

				// if the tree is in memory just load it
				if (mTrees.containsKey(mLanguage)) {
					mTree = mTrees.get(mLanguage);
					Log.i(TAG, "Tree '" + mLanguage + "' loaded from memory");
					return;
				}

				// if not, read a cached serialized version (if exists)
				try {
					mTree = unserialize();
					Log.i(TAG, "Tree '" + mLanguage
							+ "' loaded from serialized version");
					return;
				} catch (IOException e) {
					// e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
				} catch (Throwable e) {
					// e.printStackTrace();
				}
				Log.i(TAG, "Failed to read the Tree '" + mLanguage
						+ "' from serialized version");

				// if not, create new tree
				mTree = new ConcurrentRadixTree<String>(
						new DefaultCharArrayNodeFactory());
				XmlResourceParser parser = mResources.getXml(mResId);

				try {
					XmlUtils.beginDocument(parser, "words");
					String element = null;
					String search = null;
					while (true) {
						XmlUtils.nextElement(parser);
						element = parser.getName();
						if (element == null || !(element.equals("word"))) {
							break;
						}
						search = parser.getAttributeValue(null, "src");
						if (parser.next() == XmlPullParser.TEXT) {
							mTree.put(search, parser.getText());
						}
					}
					Log.i(TAG, "Tree '" + mLanguage
							+ "' created from autotext.xml");
				} catch (XmlPullParserException e) {
					Log.i(TAG, "Failed to create tree '" + mLanguage
							+ "' from autotext.xml");
					throw new RuntimeException(e);
				} catch (IOException e) {
					Log.i(TAG, "Failed to create tree '" + mLanguage
							+ "' from autotext.xml");
					throw new RuntimeException(e);
				} finally {
					parser.close();
				}

				// in an static object
				mTrees.put(mLanguage, mTree);
				Log.i(TAG, "Tree '" + mLanguage + "' saved to static object");

				// serializing the result
				try {
					serialize(mTree);
					Log.i(TAG, "Tree '" + mLanguage
							+ "' saved to serialized file");
				} catch (IOException e1) {
					Log.i(TAG, "Failed to save serialized Tree '" + mLanguage
							+ "' to file");
				} catch (Throwable e) {
					// e.printStackTrace();
				}

				return;
			}

		}, "ASK Autotext Thread", 262144).start();

	}

	public String lookup(CharSequence src, final int start, final int end) {
		// the tree loads in an async task, then maybe is null if the task takes
		// time
		if (mTree == null)
			return null;
		else
			return mTree.getValueForExactKey(src.toString());
	}

	// serialize
	public boolean serialize(ConcurrentRadixTree<String> obj)
			throws IOException {

		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + mFileDirname);
		dir.mkdirs();
		File file = new File(dir, mLanguage + ".serialized");
		if (file.exists())
			file.delete();

		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream os = new ObjectOutputStream(f);
			os.writeObject(obj);
			os.close();
			f.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// unserialize
	@SuppressWarnings("unchecked")
	public ConcurrentRadixTree<String> unserialize() throws IOException,
			ClassNotFoundException {

		File root = Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + mFileDirname);
		dir.mkdirs();
		File file = new File(dir, mLanguage + ".serialized");

		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream is = new ObjectInputStream(fis);
		ConcurrentRadixTree<String> simpleClass = (ConcurrentRadixTree<String>) is
				.readObject();
		is.close();
		fis.close();
		return simpleClass;
	}
}
