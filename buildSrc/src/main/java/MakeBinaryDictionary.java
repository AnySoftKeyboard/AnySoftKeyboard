/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2016 AnySoftKeyboard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compresses a list of words and frequencies into a tree structured binary dictionary.
 */
class MakeBinaryDictionary {
    private static final int ALPHA_SIZE = 256;
    private static final String TAG_WORD = "w";
    private static final String ATTR_FREQ = "f";
    private static final CharNode EMPTY_NODE = new CharNode();
    private static final int CHAR_WIDTH = 8;
    private static final int FLAGS_WIDTH = 1; // Terminal flag (word end)
    private static final int ADDR_WIDTH = 23; // Offset to children
    private static final int FREQ_WIDTH_BYTES = 1;
    private static final int COUNT_WIDTH_BYTES = 1;
    private static final int FLAG_ADDRESS_MASK = 0x400000;
    private static final int FLAG_TERMINAL_MASK = 0x800000;
    private static final int ADDRESS_MASK = 0x3FFFFF;
    private final String srcFilename;
    private final String destFilename;
    private List<CharNode> roots;
    private Map<String, Integer> mDictionary;
    private int mWordCount;
    private byte[] dict;
    private int dictSize;
    private int nullChildrenCount = 0;
    private int notTerminalCount = 0;

    public MakeBinaryDictionary(String srcFilename, String destFilename) {
        this.srcFilename = srcFilename;
        this.destFilename = destFilename;
    }

    public void makeDictionary() throws ParserConfigurationException, SAXException, IOException {
        populateDictionary(srcFilename);
        writeToDict(destFilename);
    }

    private void populateDictionary(String filename) throws IOException, SAXException, ParserConfigurationException {
        roots = new ArrayList<>();
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new File(filename), new DefaultHandler() {
            boolean inWord;
            int freq;
            StringBuilder wordBuilder = new StringBuilder(48);

            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) {
                if (qName.equals(TAG_WORD)) {
                    inWord = true;
                    freq = Integer.parseInt(attributes.getValue(0));
                    wordBuilder.setLength(0);
                }
            }

            @Override
            public void characters(char[] data, int offset, int length) {
                // Ignore other whitespace
                if (!inWord) return;
                wordBuilder.append(data, offset, length);
            }

            @Override
            public void endElement(String uri, String localName,
                                   String qName) {
                if (qName.equals(TAG_WORD)) {
                    if (wordBuilder.length() > 1) {
                        addWordTop(wordBuilder.toString(), freq);
                        mWordCount++;
                    }
                    inWord = false;
                }
            }
        });

        System.out.println("Nodes = " + CharNode.sNodes);
    }

    private int indexOf(List<CharNode> children, char c) {
        if (children == null) {
            return -1;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).data == c) {
                return i;
            }
        }
        return -1;
    }

    private void addWordTop(String word, int occur) {
        if (occur > 255) occur = 255;
        char firstChar = word.charAt(0);
        int index = indexOf(roots, firstChar);
        if (index == -1) {
            CharNode newNode = new CharNode();
            newNode.data = firstChar;
            newNode.freq = occur;
            index = roots.size();
            roots.add(newNode);
        } else {
            roots.get(index).freq += occur;
        }
        if (word.length() > 1) {
            addWordRec(roots.get(index), word, 1, occur);
        } else {
            roots.get(index).terminal = true;
        }
    }

    private void addWordRec(CharNode parent, String word, int charAt, int occur) {
        CharNode child = null;
        char data = word.charAt(charAt);
        if (parent.children == null) {
            parent.children = new ArrayList<>();
        } else {
            for (int i = 0; i < parent.children.size(); i++) {
                CharNode node = parent.children.get(i);
                if (node.data == data) {
                    child = node;
                    break;
                }
            }
        }
        if (child == null) {
            child = new CharNode();
            parent.children.add(child);
        }
        child.data = data;
        if (child.freq == 0) child.freq = occur;
        if (word.length() > charAt + 1) {
            addWordRec(child, word, charAt + 1, occur);
        } else {
            child.terminal = true;
            child.freq = occur;
        }
    }

    private void addCount(int count) {
        dict[dictSize++] = (byte) (0xFF & count);
    }

    private void addNode(CharNode node) {
        int charData = 0xFFFF & node.data;
        if (charData > 254) {
            dict[dictSize++] = (byte) 255;
            dict[dictSize++] = (byte) ((node.data >> 8) & 0xFF);
            dict[dictSize++] = (byte) (node.data & 0xFF);
        } else {
            dict[dictSize++] = (byte) (0xFF & node.data);
        }
        if (node.children != null) {
            dictSize += 3; // Space for children address
        } else {
            dictSize += 1; // Space for just the terminal/address flags
        }
        if ((0xFFFFFF & node.freq) > 255) {
            node.freq = 255;
        }
        if (node.terminal) {
            byte freq = (byte) (0xFF & node.freq);
            dict[dictSize++] = freq;
        }
    }

    private void updateNodeAddress(int nodeAddress, CharNode node,
                                   int childrenAddress) {
        if ((dict[nodeAddress] & 0xFF) == 0xFF) { // 3 byte character
            nodeAddress += 2;
        }
        childrenAddress = ADDRESS_MASK & childrenAddress;
        if (childrenAddress == 0) {
            nullChildrenCount++;
        } else {
            childrenAddress |= FLAG_ADDRESS_MASK;
        }
        if (node.terminal) {
            childrenAddress |= FLAG_TERMINAL_MASK;
        } else {
            notTerminalCount++;
        }
        dict[nodeAddress + 1] = (byte) (childrenAddress >> 16);
        if ((childrenAddress & FLAG_ADDRESS_MASK) != 0) {
            dict[nodeAddress + 2] = (byte) ((childrenAddress & 0xFF00) >> 8);
            dict[nodeAddress + 3] = (byte) ((childrenAddress & 0xFF));
        }
    }

    void writeWordsRec(List<CharNode> children) {
        if (children == null || children.size() == 0) {
            return;
        }
        final int childCount = children.size();
        addCount(childCount);
        //int childrenStart = dictSize;
        int[] childrenAddresses = new int[childCount];
        for (int j = 0; j < childCount; j++) {
            CharNode node = children.get(j);
            childrenAddresses[j] = dictSize;
            addNode(node);
        }
        for (int j = 0; j < childCount; j++) {
            CharNode node = children.get(j);
            int nodeAddress = childrenAddresses[j];
            int cacheDictSize = dictSize;
            writeWordsRec(node.children);
            updateNodeAddress(nodeAddress, node, node.children != null
                    ? cacheDictSize : 0);
        }
    }

    private void writeToDict(String dictFilename) {
        // 4MB max, 22-bit offsets
        dict = new byte[4 * 1024 * 1024];
        dictSize = 0;
        writeWordsRec(roots);
        System.out.println("Dict Size = " + dictSize);
        try {
            FileOutputStream fos = new FileOutputStream(dictFilename);
            fos.write(dict, 0, dictSize);
            fos.close();
        } catch (IOException ioe) {
            System.err.println("Error writing dict file:" + ioe);
        }
    }

    private static class CharNode {
        static int sNodes;
        char data;
        int freq;
        boolean terminal;
        List<CharNode> children;

        CharNode() {
            sNodes++;
        }
    }
}