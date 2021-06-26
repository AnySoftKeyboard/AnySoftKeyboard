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

package com.anysoftkeyboard.dictionaries;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import com.anysoftkeyboard.utils.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/** This class accesses a dictionary of corrections to frequent misspellings. */
public class AutoTextImpl implements AutoText {
    // struct trie {
    //     char c;
    //     int off;
    //     struct trie *child;
    //     struct trie *next;
    // };

    private static final int TRIE_C = 0;
    private static final int TRIE_OFF = 1;
    private static final int TRIE_CHILD = 2;
    private static final int TRIE_NEXT = 3;

    private static final char TRIE_SIZEOF = 4;
    private static final char TRIE_NULL = (char) -1;
    private static final char TRIE_ROOT = 0;

    private static final int INCREMENT = 1024;

    private static final int DEFAULT = 14337; // Size of the Trie 13 Aug 2007

    private static final int RIGHT = 9300; // Size of 'right' 13 Aug 2007

    //    private static AutoText sInstance =  null;//new AutoText(Resources.getSystem());
    //    private static final Object sLock = new Object();

    // TODO:
    //
    // Note the assumption that the destination strings total less than
    // 64K characters and that the trie for the source side totals less
    // than 64K chars/offsets/child pointers/next pointers.
    //
    // This seems very safe for English (currently 7K of destination,
    // 14K of trie) but may need to be revisited.

    private char[] mTrie;
    private char mTrieUsed;
    private final String mText;
    // private Locale mLocale;
    // private int mSize;

    AutoTextImpl(Resources resources, int resId) {
        // mLocale = locale;
        // init(resources);

        try (final XmlResourceParser parser = resources.getXml(resId)) {
            StringBuilder right = new StringBuilder(RIGHT);
            mTrie = new char[DEFAULT];
            mTrie[TRIE_ROOT] = TRIE_NULL;
            mTrieUsed = TRIE_ROOT + 1;

            try {
                XmlUtils.beginDocument(parser, "words");
                String odest = "";
                char ooff = 0;

                while (true) {
                    if (!XmlUtils.nextElement(parser)) {
                        // we reached the end of the parser.
                        break;
                    }

                    String element = parser.getName();
                    if (element == null || !element.equals("word")) {
                        break;
                    }

                    String src = parser.getAttributeValue(null, "src");
                    if (parser.next() == XmlPullParser.TEXT) {
                        String dest = parser.getText();
                        char off;

                        if (dest.equals(odest)) {
                            off = ooff;
                        } else {
                            off = (char) right.length();
                            right.append((char) dest.length());
                            right.append(dest);
                        }

                        add(src, off);
                    }
                }
            } catch (XmlPullParserException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            mText = right.toString();
        }
    }

    @Override
    public String lookup(CharSequence src) {
        int here = mTrie[TRIE_ROOT];

        final int length = src.length();
        for (int i = 0; i < length; i++) {
            char c = src.charAt(i);

            for (; here != TRIE_NULL; here = mTrie[here + TRIE_NEXT]) {
                if (c == mTrie[here + TRIE_C]) {
                    if ((i == length - 1) && (mTrie[here + TRIE_OFF] != TRIE_NULL)) {
                        int off = mTrie[here + TRIE_OFF];
                        int len = mText.charAt(off);

                        return mText.substring(off + 1, off + 1 + len);
                    }

                    here = mTrie[here + TRIE_CHILD];
                    break;
                }
            }

            if (here == TRIE_NULL) {
                return null;
            }
        }

        return null;
    }

    private void add(String src, char off) {
        int slen = src.length();
        int herep = TRIE_ROOT;
        // Keep track of the size of the dictionary
        // mSize++;

        for (int i = 0; i < slen; i++) {
            char c = src.charAt(i);
            boolean found = false;

            for (; mTrie[herep] != TRIE_NULL; herep = mTrie[herep] + TRIE_NEXT) {
                if (c == mTrie[mTrie[herep] + TRIE_C]) {
                    // There is a node for this letter, and this is the
                    // end, so fill in the right hand side fields.

                    if (i == slen - 1) {
                        mTrie[mTrie[herep] + TRIE_OFF] = off;
                        return;
                    }

                    // There is a node for this letter, and we need
                    // to go deeper into it to fill in the rest.

                    herep = mTrie[herep] + TRIE_CHILD;
                    found = true;
                    break;
                }
            }

            if (!found) {
                // No node for this letter yet.  Make one.

                char node = newTrieNode();
                mTrie[herep] = node;

                mTrie[mTrie[herep] + TRIE_C] = c;
                mTrie[mTrie[herep] + TRIE_OFF] = TRIE_NULL;
                mTrie[mTrie[herep] + TRIE_NEXT] = TRIE_NULL;
                mTrie[mTrie[herep] + TRIE_CHILD] = TRIE_NULL;

                // If this is the end of the word, fill in the offset.

                if (i == slen - 1) {
                    mTrie[mTrie[herep] + TRIE_OFF] = off;
                    return;
                }

                // Otherwise, step in deeper and go to the next letter.

                herep = mTrie[herep] + TRIE_CHILD;
            }
        }
    }

    private char newTrieNode() {
        if (mTrieUsed + TRIE_SIZEOF > mTrie.length) {
            char[] copy = new char[mTrie.length + INCREMENT];
            System.arraycopy(mTrie, 0, copy, 0, mTrie.length);
            mTrie = copy;
        }

        char ret = mTrieUsed;
        mTrieUsed += TRIE_SIZEOF;

        return ret;
    }
}
