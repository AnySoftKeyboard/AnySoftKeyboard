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

package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.base.Charsets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Makes writing XML much much easier.
 *
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @author <a href="mailto:menny|AT| evendanan{dot} net">Menny Even Danan - just
 * added some features on Henri's initial version</a>
 * @version 0.2
 */
public class XmlWriter {

    private static final String INDENT_STRING = "    ";
    private final boolean mThisIsWriterOwner;// is this instance the owner?
    private final Writer mWriter; // underlying mWriter
    private final int mIndentingOffset;
    private final Deque<String> mStack; // of xml entity names
    private final StringBuilder mAttrs; // current attribute string
    private boolean mEmpty; // is the current node mEmpty
    private boolean mJustWroteText;
    private boolean mClosed; // is the current node mClosed...

    /**
     * Create an XmlWriter on top of an existing java.io.Writer.
     *
     * @throws IOException
     */
    public XmlWriter(Writer writer, boolean takeOwnership, int indentingOffset, boolean addXmlPrefix)
            throws IOException {
        mThisIsWriterOwner = takeOwnership;
        this.mIndentingOffset = indentingOffset;
        this.mWriter = writer;
        this.mClosed = true;
        this.mStack = new ArrayDeque<>();
        this.mAttrs = new StringBuilder();
        if (addXmlPrefix)
            this.mWriter.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    }

    public XmlWriter(File outputFile) throws IOException {
        this(new OutputStreamWriter(new FileOutputStream(outputFile, false), Charsets.UTF8), true, 0, true);
    }

    /**
     * Begin to output an entity.
     *
     * @param name name of entity.
     */
    public XmlWriter writeEntity(String name) throws IOException {
        closeOpeningTag(true);
        this.mClosed = false;
        for (int tabIndex = 0; tabIndex < mStack.size() + mIndentingOffset; tabIndex++)
            this.mWriter.write(INDENT_STRING);
        this.mWriter.write("<");
        this.mWriter.write(name);
        mStack.push(name);
        this.mEmpty = true;
        this.mJustWroteText = false;
        return this;
    }

    // close off the opening tag
    private void closeOpeningTag(final boolean newLine) throws IOException {
        if (!this.mClosed) {
            writeAttributes();
            this.mClosed = true;
            this.mWriter.write(">");
            if (newLine)
                this.mWriter.write("\n");
        }
    }

    // write out all current attributes
    private void writeAttributes() throws IOException {
        this.mWriter.write(this.mAttrs.toString());
        this.mAttrs.setLength(0);
        this.mEmpty = false;
    }

    /**
     * Write an attribute out for the current entity. Any xml characters in the
     * value are escaped. Currently it does not actually throw the exception,
     * but the api is set that way for future changes.
     *
     * @param attr  name of attribute.
     * @param value value of attribute.
     */
    public XmlWriter writeAttribute(String attr, String value) {
        this.mAttrs.append(" ");
        this.mAttrs.append(attr);
        this.mAttrs.append("=\"");
        this.mAttrs.append(escapeXml(value));
        this.mAttrs.append("\"");
        return this;
    }

    /**
     * End the current entity. This will throw an exception if it is called when
     * there is not a currently open entity.
     *
     * @throws IOException
     */
    public XmlWriter endEntity() throws IOException {
        if (mStack.size() == 0) {
            throw new InvalidObjectException("Called endEntity too many times. ");
        }
        String name = mStack.pop();
        if (mEmpty) {
            writeAttributes();
            mWriter.write("/>\n");
        } else {
            if (!mJustWroteText) {
                for (int tabIndex = 0; tabIndex < mStack.size() + mIndentingOffset; tabIndex++)
                    mWriter.write(INDENT_STRING);
            }
            mWriter.write("</");
            mWriter.write(name);
            mWriter.write(">\n");
        }
        mEmpty = false;
        mClosed = true;
        mJustWroteText = false;
        return this;
    }

    /**
     * Close this mWriter. It does not close the underlying mWriter, but does
     * throw an exception if there are as yet unclosed tags.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (mThisIsWriterOwner) {
            this.mWriter.flush();
            this.mWriter.close();
        }
        if (this.mStack.size() > 0) {
            throw new IllegalStateException("Tags are not all closed. Possibly, " + this.mStack.pop() + " is unclosed. ");
        }
    }

    /**
     * Output body text. Any xml characters are escaped.
     */
    public XmlWriter writeText(String text) throws IOException {
        closeOpeningTag(false);
        this.mEmpty = false;
        this.mJustWroteText = true;
        this.mWriter.write(escapeXml(text));
        return this;
    }

    // Static functions lifted from generationjava helper classes
    // to make the jar smaller.

    // from XmlW
    private static String escapeXml(String str) {
        str = replaceString(str, "&", "&amp;");
        str = replaceString(str, "<", "&lt;");
        str = replaceString(str, ">", "&gt;");
        str = replaceString(str, "\"", "&quot;");
        str = replaceString(str, "'", "&apos;");
        return str;
    }

    private static String replaceString(String text, String repl, String with) {
        return replaceString(text, repl, with, -1);
    }

    /**
     * Replace a string with another string inside a larger string, for the
     * first n values of the search string.
     *
     * @param text String to do search and replace in
     * @param repl String to search for
     * @param with String to replace with
     * @param max  int values to replace
     * @return String with n values replacEd
     */
    private static String replaceString(String text, String repl, String with, int max) {
        if (text == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(text.length());
        int start = 0;
        int end;
        while ((end = text.indexOf(repl, start)) != -1) {
            buffer.append(text.substring(start, end)).append(with);
            start = end + repl.length();

            if (--max == 0) {
                break;
            }
        }
        buffer.append(text.substring(start));

        return buffer.toString();
    }
}
