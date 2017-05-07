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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlUtils {

    public static void skipCurrentTag(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && (type != XmlPullParser.END_TAG
                || parser.getDepth() > outerDepth)) {
        }
    }

    public static int convertValueToList(CharSequence value, String[] options, int defaultValue) {
        if (null != value) {
            for (int i = 0; i < options.length; i++) {
                if (value.equals(options[i]))
                    return i;
            }
        }

        return defaultValue;
    }

    public static boolean convertValueToBoolean(CharSequence value, boolean defaultValue) {
        boolean result = false;

        if (null == value)
            return defaultValue;

        if (value.equals("1")
                || value.equals("true")
                || value.equals("TRUE"))
            result = true;

        return result;
    }

    public static int convertValueToInt(CharSequence charSeq, int defaultValue) {
        if (null == charSeq)
            return defaultValue;

        String nm = charSeq.toString();

        // XXX This code is copied from Integer.decode() so we don't
        // have to instantiate an Integer!

        int value;
        int sign = 1;
        int index = 0;
        int len = nm.length();
        int base = 10;

        if ('-' == nm.charAt(0)) {
            sign = -1;
            index++;
        }

        if ('0' == nm.charAt(index)) {
            //  Quick check for a zero by itself
            if (index == (len - 1))
                return 0;

            char c = nm.charAt(index + 1);

            if ('x' == c || 'X' == c) {
                index += 2;
                base = 16;
            } else {
                index++;
                base = 8;
            }
        } else if ('#' == nm.charAt(index)) {
            index++;
            base = 16;
        }

        return Integer.parseInt(nm.substring(index), base) * sign;
    }

    public static int convertValueToUnsignedInt(String value, int defaultValue) {
        if (null == value)
            return defaultValue;

        return parseUnsignedIntAttribute(value);
    }

    public static int parseUnsignedIntAttribute(CharSequence charSeq) {
        String value = charSeq.toString();

        long bits;
        int index = 0;
        int len = value.length();
        int base = 10;

        if ('0' == value.charAt(index)) {
            //  Quick check for zero by itself
            if (index == (len - 1))
                return 0;

            char c = value.charAt(index + 1);

            if ('x' == c || 'X' == c) {     //  check for hex
                index += 2;
                base = 16;
            } else {                        //  check for octal
                index++;
                base = 8;
            }
        } else if ('#' == value.charAt(index)) {
            index++;
            base = 16;
        }

        return (int) Long.parseLong(value.substring(index), base);
    }

    /**
     * Flatten a Map into an output stream as XML.  The map can later be
     * read back with readMapXml().
     *
     * @param val The map to be flattened.
     * @param out Where to write the XML data.
     *
     * @see #writeMapXml(Map, String, XmlSerializer)
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     */
//    public static final void writeMapXml(Map val, OutputStream out)
//            throws XmlPullParserException, java.io.IOException {
//        XmlSerializer serializer = new FastXmlSerializer();
//        serializer.setOutput(out, "utf-8");
//        serializer.startDocument(null, true);
//        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
//        writeMapXml(val, null, serializer);
//        serializer.endDocument();
//    }

    /**
     * Flatten a List into an output stream as XML.  The list can later be
     * read back with readListXml().
     *
     * @param val The list to be flattened.
     * @param out Where to write the XML data.
     * @see #writeListXml(List, String, XmlSerializer)
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readListXml
     */
    public static void writeListXml(List val, OutputStream out)
            throws XmlPullParserException, java.io.IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(out, "utf-8");
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        writeListXml(val, null, serializer);
        serializer.endDocument();
    }

    /**
     * Flatten a Map into an XmlSerializer.  The map can later be read back
     * with readThisMapXml().
     *
     * @param val  The map to be flattened.
     * @param name Name attribute to include with this list's tag, or null for
     *             none.
     * @param out  XmlSerializer to write the map into.
     * @see #writeListXml
     * @see #writeValueXml
     * @see #readMapXml
     */
    public static void writeMapXml(Map val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        Set s = val.entrySet();
        Iterator i = s.iterator();

        out.startTag(null, "map");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            writeValueXml(e.getValue(), (String) e.getKey(), out);
        }

        out.endTag(null, "map");
    }

    /**
     * Flatten a List into an XmlSerializer.  The list can later be read back
     * with readThisListXml().
     *
     * @param val  The list to be flattened.
     * @param name Name attribute to include with this list's tag, or null for
     *             none.
     * @param out  XmlSerializer to write the list into.
     * @see #writeListXml(List, OutputStream)
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readListXml
     */
    public static void writeListXml(List val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "list");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        int size = val.size();
        int i = 0;
        while (i < size) {
            writeValueXml(val.get(i), null, out);
            i++;
        }

        out.endTag(null, "list");
    }

    public static void writeSetXml(Set val, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "set");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        for (Object v : val) {
            writeValueXml(v, null, out);
        }

        out.endTag(null, "set");
    }

    /**
     * Flatten a byte[] into an XmlSerializer.  The list can later be read back
     * with readThisByteArrayXml().
     *
     * @param val  The byte array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out  XmlSerializer to write the array into.
     * @see #writeMapXml
     * @see #writeValueXml
     */
    public static void writeByteArrayXml(byte[] val, String name,
                                         XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "byte-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", Integer.toString(N));

        StringBuilder sb = new StringBuilder(val.length * 2);
        for (byte b : val) {
            int h = b >> 4;
            sb.append(h >= 10 ? ('a' + h - 10) : ('0' + h));
            h = b & 0xff;
            sb.append(h >= 10 ? ('a' + h - 10) : ('0' + h));
        }

        out.text(sb.toString());

        out.endTag(null, "byte-array");
    }

    /**
     * Flatten an int[] into an XmlSerializer.  The list can later be read back
     * with readThisIntArrayXml().
     *
     * @param val  The int array to be flattened.
     * @param name Name attribute to include with this array's tag, or null for
     *             none.
     * @param out  XmlSerializer to write the array into.
     * @see #writeMapXml
     * @see #writeValueXml
     * @see #readThisIntArrayXml
     */
    public static void writeIntArrayXml(int[] val, String name,
                                        XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "int-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", Integer.toString(N));

        for (int aVal : val) {
            out.startTag(null, "item");
            out.attribute(null, "value", Integer.toString(aVal));
            out.endTag(null, "item");
        }

        out.endTag(null, "int-array");
    }

    /**
     * Flatten an object's value into an XmlSerializer.  The value can later
     * be read back with readThisValueXml().
     * <p/>
     * Currently supported value types are: null, String, Integer, Long,
     * Float, Double Boolean, Map, List.
     *
     * @param v    The object to be flattened.
     * @param name Name attribute to include with this value's tag, or null
     *             for none.
     * @param out  XmlSerializer to write the object into.
     * @see #writeMapXml
     * @see #writeListXml
     * @see #readValueXml
     */
    public static void writeValueXml(Object v, String name, XmlSerializer out)
            throws XmlPullParserException, java.io.IOException {
        String typeStr;
        if (v == null) {
            out.startTag(null, "null");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.endTag(null, "null");
            return;
        } else if (v instanceof String) {
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else if (v instanceof Integer) {
            typeStr = "int";
        } else if (v instanceof Long) {
            typeStr = "long";
        } else if (v instanceof Float) {
            typeStr = "float";
        } else if (v instanceof Double) {
            typeStr = "double";
        } else if (v instanceof Boolean) {
            typeStr = "boolean";
        } else if (v instanceof byte[]) {
            writeByteArrayXml((byte[]) v, name, out);
            return;
        } else if (v instanceof int[]) {
            writeIntArrayXml((int[]) v, name, out);
            return;
        } else if (v instanceof Map) {
            writeMapXml((Map) v, name, out);
            return;
        } else if (v instanceof List) {
            writeListXml((List) v, name, out);
            return;
        } else if (v instanceof Set) {
            writeSetXml((Set) v, name, out);
            return;
        } else if (v instanceof CharSequence) {
            // XXX This is to allow us to at least write something if
            // we encounter styled text...  but it means we will drop all
            // of the styling information. :(
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else {
            throw new RuntimeException("writeValueXml: unable to write value " + v);
        }

        out.startTag(null, typeStr);
        if (name != null) {
            out.attribute(null, "name", name);
        }
        out.attribute(null, "value", v.toString());
        out.endTag(null, typeStr);
    }

    /**
     * Read a HashMap from an InputStream containing XML.  The stream can
     * previously have been written by writeMapXml().
     *
     * @param in The InputStream from which to read.
     * @return HashMap The resulting map.
     * @see #readListXml
     * @see #readValueXml
     * @see #readThisMapXml
     * #see #writeMapXml
     */
    public static HashMap readMapXml(InputStream in)
            throws XmlPullParserException, java.io.IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        return (HashMap) readValueXml(parser, new String[1]);
    }

    /**
     * Read an ArrayList from an InputStream containing XML.  The stream can
     * previously have been written by writeListXml().
     *
     * @param in The InputStream from which to read.
     * @return ArrayList The resulting list.
     * @see #readMapXml
     * @see #readValueXml
     * @see #readThisListXml
     * @see #writeListXml
     */
    public static ArrayList readListXml(InputStream in)
            throws XmlPullParserException, java.io.IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        return (ArrayList) readValueXml(parser, new String[1]);
    }


    /**
     * Read a HashSet from an InputStream containing XML. The stream can
     * previously have been written by writeSetXml().
     *
     * @param in The InputStream from which to read.
     * @return HashSet The resulting set.
     * @throws XmlPullParserException
     * @throws java.io.IOException
     * @see #readValueXml
     * @see #readThisSetXml
     * @see #writeSetXml
     */
    public static HashSet readSetXml(InputStream in)
            throws XmlPullParserException, java.io.IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        return (HashSet) readValueXml(parser, new String[1]);
    }

    /**
     * Read a HashMap object from an XmlPullParser.  The XML data could
     * previously have been generated by writeMapXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the map.
     *
     * @param parser The XmlPullParser from which to read the map data.
     * @param endTag Name of the tag that will end the map, usually "map".
     * @param name   An array of one string, used to return the name attribute
     *               of the map's tag.
     * @return HashMap The newly generated map.
     * @see #readMapXml
     */
    public static HashMap readThisMapXml(XmlPullParser parser, String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        HashMap<String, Object> map = new HashMap<>();

        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                if (name[0] != null) {
                    map.put(name[0], val);
                } else {
                    throw new XmlPullParserException(
                            "Map value without name attribute: " + parser.getName());
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return map;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }

    /**
     * Read an ArrayList object from an XmlPullParser.  The XML data could
     * previously have been generated by writeListXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name   An array of one string, used to return the name attribute
     *               of the list's tag.
     * @return HashMap The newly generated list.
     * @see #readListXml
     */
    public static ArrayList readThisListXml(XmlPullParser parser, String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        ArrayList<Object> list = new ArrayList<>();

        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                list.add(val);
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return list;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }

    /**
     * Read a HashSet object from an XmlPullParser. The XML data could previously
     * have been generated by writeSetXml(). The XmlPullParser must be positioned
     * <em>after</em> the tag that begins the set.
     *
     * @param parser The XmlPullParser from which to read the set data.
     * @param endTag Name of the tag that will end the set, usually "set".
     * @param name   An array of one string, used to return the name attribute
     *               of the set's tag.
     * @return HashSet The newly generated set.
     * @throws XmlPullParserException
     * @throws java.io.IOException
     * @see #readSetXml
     */
    public static HashSet readThisSetXml(XmlPullParser parser, String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {
        HashSet<Object> set = new HashSet<>();

        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                set.add(val);
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return set;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }

    /**
     * Read an int[] object from an XmlPullParser.  The XML data could
     * previously have been generated by writeIntArrayXml().  The XmlPullParser
     * must be positioned <em>after</em> the tag that begins the list.
     *
     * @param parser The XmlPullParser from which to read the list data.
     * @param endTag Name of the tag that will end the list, usually "list".
     * @param name   An array of one string, used to return the name attribute
     *               of the list's tag.
     * @return Returns a newly generated int[].
     * @see #readListXml
     */
    public static int[] readThisIntArrayXml(XmlPullParser parser, String endTag, String[] name)
            throws XmlPullParserException, java.io.IOException {

        int num;
        try {
            num = Integer.parseInt(parser.getAttributeValue(null, "num"));
        } catch (NullPointerException e) {
            throw new XmlPullParserException(
                    "Need num attribute in byte-array");
        } catch (NumberFormatException e) {
            throw new XmlPullParserException(
                    "Not a number in num attribute in byte-array");
        }

        int[] array = new int[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("item")) {
                    try {
                        array[i] = Integer.parseInt(
                                parser.getAttributeValue(null, "value"));
                    } catch (NullPointerException e) {
                        throw new XmlPullParserException(
                                "Need value attribute in item");
                    } catch (NumberFormatException e) {
                        throw new XmlPullParserException(
                                "Not a number in value attribute in item");
                    }
                } else {
                    throw new XmlPullParserException(
                            "Expected item tag at: " + parser.getName());
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException(
                            "Expected " + endTag + " end tag at: "
                                    + parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }

    /**
     * Read a flattened object from an XmlPullParser.  The XML data could
     * previously have been written with writeMapXml(), writeListXml(), or
     * writeValueXml().  The XmlPullParser must be positioned <em>at</em> the
     * tag that defines the value.
     *
     * @param parser The XmlPullParser from which to read the object.
     * @param name   An array of one string, used to return the name attribute
     *               of the value's tag.
     * @return Object The newly generated value object.
     * @see #readMapXml
     * @see #readListXml
     * @see #writeValueXml
     */
    public static Object readValueXml(XmlPullParser parser, String[] name)
            throws XmlPullParserException, java.io.IOException {
        int eventType = parser.getEventType();
        do {
            if (eventType == XmlPullParser.START_TAG) {
                return readThisValueXml(parser, name);
            } else if (eventType == XmlPullParser.END_TAG) {
                throw new XmlPullParserException(
                        "Unexpected end tag at: " + parser.getName());
            } else if (eventType == XmlPullParser.TEXT) {
                throw new XmlPullParserException(
                        "Unexpected text: " + parser.getText());
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Unexpected end of document");
    }

    private static Object readThisValueXml(XmlPullParser parser, String[] name)
            throws XmlPullParserException, java.io.IOException {
        final String valueName = parser.getAttributeValue(null, "name");
        final String tagName = parser.getName();

        Object res;

        switch (tagName) {
            case "null":
                res = null;
                break;
            case "string":
                String value = "";
                int eventType;
                while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.END_TAG) {
                        if (parser.getName().equals("string")) {
                            name[0] = valueName;
                            return value;
                        }
                        throw new XmlPullParserException("Unexpected end tag in <string>: " + parser.getName());
                    } else if (eventType == XmlPullParser.TEXT) {
                        value += parser.getText();
                    } else if (eventType == XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("Unexpected start tag in <string>: " + parser.getName());
                    }
                }
                throw new XmlPullParserException(
                        "Unexpected end of document in <string>");
            case "int":
                res = Integer.parseInt(parser.getAttributeValue(null, "value"));
                break;
            case "long":
                res = Long.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "float":
                res = Float.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "double":
                res = Double.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "boolean":
                res = Boolean.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "int-array":
                parser.next();
                res = readThisIntArrayXml(parser, "int-array", name);
                name[0] = valueName;
                return res;
            case "map":
                parser.next();
                res = readThisMapXml(parser, "map", name);
                name[0] = valueName;
                return res;
            case "list":
                parser.next();
                res = readThisListXml(parser, "list", name);
                name[0] = valueName;
                return res;
            case "set":
                parser.next();
                res = readThisSetXml(parser, "set", name);
                name[0] = valueName;
                return res;
            default:
                throw new XmlPullParserException(
                        "Unknown tag: " + tagName);
        }

        // Skip through to end tag.
        int eventType;
        while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals(tagName)) {
                    name[0] = valueName;
                    return res;
                }
                throw new XmlPullParserException("Unexpected end tag in <" + tagName + ">: " + parser.getName());
            } else if (eventType == XmlPullParser.TEXT) {
                throw new XmlPullParserException("Unexpected text in <" + tagName + ">: " + parser.getName());
            } else if (eventType == XmlPullParser.START_TAG) {
                throw new XmlPullParserException("Unexpected start tag in <" + tagName + ">: " + parser.getName());
            }
        }
        throw new XmlPullParserException("Unexpected end of document in <" + tagName + ">");
    }

    public static void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            ;
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public static void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            ;
        }
    }
}
