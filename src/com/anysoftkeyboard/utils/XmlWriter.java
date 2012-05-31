
package com.anysoftkeyboard.utils;

/*
 * Copyright (c) 2003, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of XmlWriter nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (c) 2010, Menny Even Danan
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of XmlWriter nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Writer;

import java.util.Stack;

/**
 * Makes writing XML much much easier.
 * 
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @author <a href="mailto:menny|AT| evendanan{dot} net">Menny Even Danan - just
 *         added some features on Henri's initial version</a>
 * @version 0.2
 */
public class XmlWriter {

    private static final String INDENT_STRING = "    ";
    private final boolean thisIsWriterOwner;// is this instance the owner?
    private final Writer writer; // underlying writer
    private final int indentingOffset;
    private final Stack<String> stack; // of xml entity names
    private final StringBuffer attrs; // current attribute string
    private boolean empty; // is the current node empty
    private boolean justWroteText;
    private boolean closed; // is the current node closed...

    /**
     * Create an XmlWriter on top of an existing java.io.Writer.
     * 
     * @throws IOException
     */
    public XmlWriter(Writer writer, boolean takeOwnership, int indentingOffset, boolean addXmlPrefix)
            throws IOException {
        thisIsWriterOwner = takeOwnership;
        this.indentingOffset = indentingOffset;
        this.writer = writer;
        this.closed = true;
        this.stack = new Stack<String>();
        this.attrs = new StringBuffer();
        if (addXmlPrefix)
            this.writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    }

    public XmlWriter(File outputFile) throws IOException {
        this(new FileWriter(outputFile), true, 0, true);
    }

    /**
     * Begin to output an entity.
     * 
     * @param String name of entity.
     */
    public XmlWriter writeEntity(String name) throws IOException {
        closeOpeningTag(true);
        this.closed = false;
        for (int tabIndex = 0; tabIndex < stack.size() + indentingOffset; tabIndex++)
            this.writer.write(INDENT_STRING);
        this.writer.write("<");
        this.writer.write(name);
        stack.add(name);
        this.empty = true;
        this.justWroteText = false;
        return this;
    }

    // close off the opening tag
    private void closeOpeningTag(final boolean newLine) throws IOException {
        if (!this.closed) {
            writeAttributes();
            this.closed = true;
            this.writer.write(">");
            if (newLine)
                this.writer.write("\n");
        }
    }

    // write out all current attributes
    private void writeAttributes() throws IOException {
        this.writer.write(this.attrs.toString());
        this.attrs.setLength(0);
        this.empty = false;
    }

    /**
     * Write an attribute out for the current entity. Any xml characters in the
     * value are escaped. Currently it does not actually throw the exception,
     * but the api is set that way for future changes.
     * 
     * @param String name of attribute.
     * @param String value of attribute.
     */
    public XmlWriter writeAttribute(String attr, String value) {
        this.attrs.append(" ");
        this.attrs.append(attr);
        this.attrs.append("=\"");
        this.attrs.append(escapeXml(value));
        this.attrs.append("\"");
        return this;
    }

    /**
     * End the current entity. This will throw an exception if it is called when
     * there is not a currently open entity.
     * 
     * @throws IOException
     */
    public XmlWriter endEntity() throws IOException {
        if (this.stack.empty()) {
            throw new InvalidObjectException("Called endEntity too many times. ");
        }
        String name = this.stack.pop();
        if (name != null) {
            if (this.empty) {
                writeAttributes();
                this.writer.write("/>\n");
            } else {
                if (!this.justWroteText)
                {
                    for (int tabIndex = 0; tabIndex < stack.size() + indentingOffset; tabIndex++)
                        this.writer.write(INDENT_STRING);
                }
                this.writer.write("</");
                this.writer.write(name);
                this.writer.write(">\n");
            }
            this.empty = false;
            this.justWroteText = false;
        }
        return this;
    }

    /**
     * Close this writer. It does not close the underlying writer, but does
     * throw an exception if there are as yet unclosed tags.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (!this.stack.empty()) {
            throw new InvalidObjectException("Tags are not all closed. " +
                    "Possibly, " + this.stack.pop() + " is unclosed. ");
        }
        if (thisIsWriterOwner)
        {
            this.writer.flush();
            this.writer.close();
        }
    }

    /**
     * Output body text. Any xml characters are escaped.
     */
    public XmlWriter writeText(String text) throws IOException {
        closeOpeningTag(false);
        this.empty = false;
        this.justWroteText = true;
        this.writer.write(escapeXml(text));
        return this;
    }

    // Static functions lifted from generationjava helper classes
    // to make the jar smaller.

    // from XmlW
    static public String escapeXml(String str) {
        str = replaceString(str, "&", "&amp;");
        str = replaceString(str, "<", "&lt;");
        str = replaceString(str, ">", "&gt;");
        str = replaceString(str, "\"", "&quot;");
        str = replaceString(str, "'", "&apos;");
        return str;
    }

    // from StringW
    static public String replaceString(String text, String repl, String with) {
        return replaceString(text, repl, with, -1);
    }

    /**
     * Replace a string with another string inside a larger string, for the
     * first n values of the search string.
     * 
     * @param text String to do search and replace in
     * @param repl String to search for
     * @param with String to replace with
     * @param n int values to replace
     * @return String with n values replacEd
     */
    static public String replaceString(String text, String repl, String with, int max) {
        if (text == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer(text.length());
        int start = 0;
        int end = 0;
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
    //
    // static public void test1() throws WritingException {
    // Writer writer = new java.io.StringWriter();
    // XmlWriter xmlwriter = new XmlWriter(writer);
    // xmlwriter.writeEntity("person").writeAttribute("name",
    // "fred").writeAttribute("age",
    // "12").writeEntity("phone").writeText("4254343").endEntity().writeEntity("bob").endEntity().endEntity();
    // xmlwriter.close();
    // System.err.println(writer.toString());
    // }
    // static public void test2() throws WritingException {
    // Writer writer = new java.io.StringWriter();
    // XmlWriter xmlwriter = new XmlWriter(writer);
    // xmlwriter.writeEntity("person");
    // xmlwriter.writeAttribute("name", "fred");
    // xmlwriter.writeAttribute("age", "12");
    // xmlwriter.writeEntity("phone");
    // xmlwriter.writeText("4254343");
    // xmlwriter.endEntity();
    // xmlwriter.writeEntity("bob");
    // xmlwriter.endEntity();
    // xmlwriter.endEntity();
    // xmlwriter.close();
    // System.err.println(writer.toString());
    // }

}
