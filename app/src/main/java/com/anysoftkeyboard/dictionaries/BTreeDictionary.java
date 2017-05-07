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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.text.TextUtils;

import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.EditableDictionary;
import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class BTreeDictionary extends EditableDictionary {

    public interface WordReadListener {
        /**
         * Callback when a word has been read from storage.
         *
         * @return true if to continue, false to stop.
         */
        boolean onWordRead(String word, int frequency);
    }


    public static final int MAX_WORD_LENGTH = 32;
    protected static final String TAG = "ASK UDict";
    private static final char QUOTE = '\'';
    private static final int INITIAL_ROOT_CAPACITY = 26/*number of letters in the English Alphabet. Why bother with auto-increment, when we can start at roughly the right final size..*/;
    /**
     * Table mapping most combined Latin, Greek, and Cyrillic characters to
     * their base characters. If c is in range, BASE_CHARS[c] == c if c is not a
     * combined character, or the base character if it is combined.
     */
    private static final char[] BASE_CHARS = {0x0000, 0x0001, 0x0002, 0x0003, 0x0004, 0x0005, 0x0006, 0x0007, 0x0008, 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x000e, 0x000f, 0x0010, 0x0011, 0x0012, 0x0013, 0x0014, 0x0015, 0x0016, 0x0017, 0x0018, 0x0019, 0x001a, 0x001b, 0x001c, 0x001d, 0x001e, 0x001f, 0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f, 0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f, 0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f, 0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f, 0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x007f, 0x0080, 0x0081, 0x0082, 0x0083, 0x0084, 0x0085, 0x0086, 0x0087, 0x0088, 0x0089, 0x008a, 0x008b, 0x008c, 0x008d, 0x008e, 0x008f, 0x0090, 0x0091, 0x0092, 0x0093, 0x0094, 0x0095, 0x0096, 0x0097, 0x0098, 0x0099, 0x009a, 0x009b, 0x009c, 0x009d, 0x009e, 0x009f, 0x0020, 0x00a1, 0x00a2, 0x00a3, 0x00a4, 0x00a5, 0x00a6, 0x00a7, 0x0020, 0x00a9, 0x0061, 0x00ab, 0x00ac, 0x00ad, 0x00ae, 0x0020, 0x00b0, 0x00b1, 0x0032, 0x0033, 0x0020, 0x03bc, 0x00b6, 0x00b7, 0x0020, 0x0031, 0x006f, 0x00bb, 0x0031, 0x0031, 0x0033, 0x00bf, 0x0041, 0x0041, 0x0041, 0x0041, 0x0041, 0x0041, 0x00c6, 0x0043, 0x0045, 0x0045, 0x0045, 0x0045, 0x0049, 0x0049, 0x0049, 0x0049, 0x00d0, 0x004e, 0x004f, 0x004f, 0x004f, 0x004f, 0x004f, 0x00d7, 0x004f, 0x0055, 0x0055, 0x0055, 0x0055, 0x0059, 0x00de, 0x0073, // Manually changed d8 to 4f
            // Manually changed df to 73
            0x0061, 0x0061, 0x0061, 0x0061, 0x0061, 0x0061, 0x00e6, 0x0063, 0x0065, 0x0065, 0x0065, 0x0065, 0x0069, 0x0069, 0x0069, 0x0069, 0x00f0, 0x006e, 0x006f, 0x006f, 0x006f, 0x006f, 0x006f, 0x00f7, 0x006f, 0x0075, 0x0075, 0x0075, 0x0075, 0x0079, 0x00fe, 0x0079, // Manually changed f8 to 6f
            0x0041, 0x0061, 0x0041, 0x0061, 0x0041, 0x0061, 0x0043, 0x0063, 0x0043, 0x0063, 0x0043, 0x0063, 0x0043, 0x0063, 0x0044, 0x0064, 0x0110, 0x0111, 0x0045, 0x0065, 0x0045, 0x0065, 0x0045, 0x0065, 0x0045, 0x0065, 0x0045, 0x0065, 0x0047, 0x0067, 0x0047, 0x0067, 0x0047, 0x0067, 0x0047, 0x0067, 0x0048, 0x0068, 0x0126, 0x0127, 0x0049, 0x0069, 0x0049, 0x0069, 0x0049, 0x0069, 0x0049, 0x0069, 0x0049, 0x0131, 0x0049, 0x0069, 0x004a, 0x006a, 0x004b, 0x006b, 0x0138, 0x004c, 0x006c, 0x004c, 0x006c, 0x004c, 0x006c, 0x004c, 0x006c, 0x0141, 0x0142, 0x004e, 0x006e, 0x004e, 0x006e, 0x004e, 0x006e, 0x02bc, 0x014a, 0x014b, 0x004f, 0x006f, 0x004f, 0x006f, 0x004f, 0x006f, 0x0152, 0x0153, 0x0052, 0x0072, 0x0052, 0x0072, 0x0052, 0x0072, 0x0053, 0x0073, 0x0053, 0x0073, 0x0053, 0x0073, 0x0053, 0x0073, 0x0054, 0x0074, 0x0054, 0x0074, 0x0166, 0x0167, 0x0055, 0x0075, 0x0055, 0x0075, 0x0055, 0x0075, 0x0055, 0x0075, 0x0055, 0x0075, 0x0055, 0x0075, 0x0057, 0x0077, 0x0059, 0x0079, 0x0059, 0x005a, 0x007a, 0x005a, 0x007a, 0x005a, 0x007a, 0x0073, 0x0180, 0x0181, 0x0182, 0x0183, 0x0184, 0x0185, 0x0186, 0x0187, 0x0188, 0x0189, 0x018a, 0x018b, 0x018c, 0x018d, 0x018e, 0x018f, 0x0190, 0x0191, 0x0192, 0x0193, 0x0194, 0x0195, 0x0196, 0x0197, 0x0198, 0x0199, 0x019a, 0x019b, 0x019c, 0x019d, 0x019e, 0x019f, 0x004f, 0x006f, 0x01a2, 0x01a3, 0x01a4, 0x01a5, 0x01a6, 0x01a7, 0x01a8, 0x01a9, 0x01aa, 0x01ab, 0x01ac, 0x01ad, 0x01ae, 0x0055, 0x0075, 0x01b1, 0x01b2, 0x01b3, 0x01b4, 0x01b5, 0x01b6, 0x01b7, 0x01b8, 0x01b9, 0x01ba, 0x01bb, 0x01bc, 0x01bd, 0x01be, 0x01bf, 0x01c0, 0x01c1, 0x01c2, 0x01c3, 0x0044, 0x0044, 0x0064, 0x004c, 0x004c, 0x006c, 0x004e, 0x004e, 0x006e, 0x0041, 0x0061, 0x0049, 0x0069, 0x004f, 0x006f, 0x0055, 0x0075, 0x00dc, 0x00fc, 0x00dc, 0x00fc, 0x00dc, 0x00fc, 0x00dc, 0x00fc, 0x01dd, 0x00c4, 0x00e4, 0x0226, 0x0227, 0x00c6, 0x00e6, 0x01e4, 0x01e5, 0x0047, 0x0067, 0x004b, 0x006b, 0x004f, 0x006f, 0x01ea, 0x01eb, 0x01b7, 0x0292, 0x006a, 0x0044, 0x0044, 0x0064, 0x0047, 0x0067, 0x01f6, 0x01f7, 0x004e, 0x006e, 0x00c5, 0x00e5, 0x00c6, 0x00e6, 0x00d8, 0x00f8, 0x0041, 0x0061, 0x0041, 0x0061, 0x0045, 0x0065, 0x0045, 0x0065, 0x0049, 0x0069, 0x0049, 0x0069, 0x004f, 0x006f, 0x004f, 0x006f, 0x0052, 0x0072, 0x0052, 0x0072, 0x0055, 0x0075, 0x0055, 0x0075, 0x0053, 0x0073, 0x0054, 0x0074, 0x021c, 0x021d, 0x0048, 0x0068, 0x0220, 0x0221, 0x0222, 0x0223, 0x0224, 0x0225, 0x0041, 0x0061, 0x0045, 0x0065, 0x00d6, 0x00f6, 0x00d5, 0x00f5, 0x004f, 0x006f, 0x022e, 0x022f, 0x0059, 0x0079, 0x0234, 0x0235, 0x0236, 0x0237, 0x0238, 0x0239, 0x023a, 0x023b, 0x023c, 0x023d, 0x023e, 0x023f, 0x0240, 0x0241, 0x0242, 0x0243, 0x0244, 0x0245, 0x0246, 0x0247, 0x0248, 0x0249, 0x024a, 0x024b, 0x024c, 0x024d, 0x024e, 0x024f, 0x0250, 0x0251, 0x0252, 0x0253, 0x0254, 0x0255, 0x0256, 0x0257, 0x0258, 0x0259, 0x025a, 0x025b, 0x025c, 0x025d, 0x025e, 0x025f, 0x0260, 0x0261, 0x0262, 0x0263, 0x0264, 0x0265, 0x0266, 0x0267, 0x0268, 0x0269, 0x026a, 0x026b, 0x026c, 0x026d, 0x026e, 0x026f, 0x0270, 0x0271, 0x0272, 0x0273, 0x0274, 0x0275, 0x0276, 0x0277, 0x0278, 0x0279, 0x027a, 0x027b, 0x027c, 0x027d, 0x027e, 0x027f, 0x0280, 0x0281, 0x0282, 0x0283, 0x0284, 0x0285, 0x0286, 0x0287, 0x0288, 0x0289, 0x028a, 0x028b, 0x028c, 0x028d, 0x028e, 0x028f, 0x0290, 0x0291, 0x0292, 0x0293, 0x0294, 0x0295, 0x0296, 0x0297, 0x0298, 0x0299, 0x029a, 0x029b, 0x029c, 0x029d, 0x029e, 0x029f, 0x02a0, 0x02a1, 0x02a2, 0x02a3, 0x02a4, 0x02a5, 0x02a6, 0x02a7, 0x02a8, 0x02a9, 0x02aa, 0x02ab, 0x02ac, 0x02ad, 0x02ae, 0x02af, 0x0068, 0x0266, 0x006a, 0x0072, 0x0279, 0x027b, 0x0281, 0x0077, 0x0079, 0x02b9, 0x02ba, 0x02bb, 0x02bc, 0x02bd, 0x02be, 0x02bf, 0x02c0, 0x02c1, 0x02c2, 0x02c3, 0x02c4, 0x02c5, 0x02c6, 0x02c7, 0x02c8, 0x02c9, 0x02ca, 0x02cb, 0x02cc, 0x02cd, 0x02ce, 0x02cf, 0x02d0, 0x02d1, 0x02d2, 0x02d3, 0x02d4, 0x02d5, 0x02d6, 0x02d7, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x02de, 0x02df, 0x0263, 0x006c, 0x0073, 0x0078, 0x0295, 0x02e5, 0x02e6, 0x02e7, 0x02e8, 0x02e9, 0x02ea, 0x02eb, 0x02ec, 0x02ed, 0x02ee, 0x02ef, 0x02f0, 0x02f1, 0x02f2, 0x02f3, 0x02f4, 0x02f5, 0x02f6, 0x02f7, 0x02f8, 0x02f9, 0x02fa, 0x02fb, 0x02fc, 0x02fd, 0x02fe, 0x02ff, 0x0300, 0x0301, 0x0302, 0x0303, 0x0304, 0x0305, 0x0306, 0x0307, 0x0308, 0x0309, 0x030a, 0x030b, 0x030c, 0x030d, 0x030e, 0x030f, 0x0310, 0x0311, 0x0312, 0x0313, 0x0314, 0x0315, 0x0316, 0x0317, 0x0318, 0x0319, 0x031a, 0x031b, 0x031c, 0x031d, 0x031e, 0x031f, 0x0320, 0x0321, 0x0322, 0x0323, 0x0324, 0x0325, 0x0326, 0x0327, 0x0328, 0x0329, 0x032a, 0x032b, 0x032c, 0x032d, 0x032e, 0x032f, 0x0330, 0x0331, 0x0332, 0x0333, 0x0334, 0x0335, 0x0336, 0x0337, 0x0338, 0x0339, 0x033a, 0x033b, 0x033c, 0x033d, 0x033e, 0x033f, 0x0300, 0x0301, 0x0342, 0x0313, 0x0308, 0x0345, 0x0346, 0x0347, 0x0348, 0x0349, 0x034a, 0x034b, 0x034c, 0x034d, 0x034e, 0x034f, 0x0350, 0x0351, 0x0352, 0x0353, 0x0354, 0x0355, 0x0356, 0x0357, 0x0358, 0x0359, 0x035a, 0x035b, 0x035c, 0x035d, 0x035e, 0x035f, 0x0360, 0x0361, 0x0362, 0x0363, 0x0364, 0x0365, 0x0366, 0x0367, 0x0368, 0x0369, 0x036a, 0x036b, 0x036c, 0x036d, 0x036e, 0x036f, 0x0370, 0x0371, 0x0372, 0x0373, 0x02b9, 0x0375, 0x0376, 0x0377, 0x0378, 0x0379, 0x0020, 0x037b, 0x037c, 0x037d, 0x003b, 0x037f, 0x0380, 0x0381, 0x0382, 0x0383, 0x0020, 0x00a8, 0x0391, 0x00b7, 0x0395, 0x0397, 0x0399, 0x038b, 0x039f, 0x038d, 0x03a5, 0x03a9, 0x03ca, 0x0391, 0x0392, 0x0393, 0x0394, 0x0395, 0x0396, 0x0397, 0x0398, 0x0399, 0x039a, 0x039b, 0x039c, 0x039d, 0x039e, 0x039f, 0x03a0, 0x03a1, 0x03a2, 0x03a3, 0x03a4, 0x03a5, 0x03a6, 0x03a7, 0x03a8, 0x03a9, 0x0399, 0x03a5, 0x03b1, 0x03b5, 0x03b7, 0x03b9, 0x03cb, 0x03b1, 0x03b2, 0x03b3, 0x03b4, 0x03b5, 0x03b6, 0x03b7, 0x03b8, 0x03b9, 0x03ba, 0x03bb, 0x03bc, 0x03bd, 0x03be, 0x03bf, 0x03c0, 0x03c1, 0x03c2, 0x03c3, 0x03c4, 0x03c5, 0x03c6, 0x03c7, 0x03c8, 0x03c9, 0x03b9, 0x03c5, 0x03bf, 0x03c5, 0x03c9, 0x03cf, 0x03b2, 0x03b8, 0x03a5, 0x03d2, 0x03d2, 0x03c6, 0x03c0, 0x03d7, 0x03d8, 0x03d9, 0x03da, 0x03db, 0x03dc, 0x03dd, 0x03de, 0x03df, 0x03e0, 0x03e1, 0x03e2, 0x03e3, 0x03e4, 0x03e5, 0x03e6, 0x03e7, 0x03e8, 0x03e9, 0x03ea, 0x03eb, 0x03ec, 0x03ed, 0x03ee, 0x03ef, 0x03ba, 0x03c1, 0x03c2, 0x03f3, 0x0398, 0x03b5, 0x03f6, 0x03f7, 0x03f8, 0x03a3, 0x03fa, 0x03fb, 0x03fc, 0x03fd, 0x03fe, 0x03ff, 0x0415, 0x0415, 0x0402, 0x0413, 0x0404, 0x0405, 0x0406, 0x0406, 0x0408, 0x0409, 0x040a, 0x040b, 0x041a, 0x0418, 0x0423, 0x040f, 0x0410, 0x0411, 0x0412, 0x0413, 0x0414, 0x0415, 0x0416, 0x0417, 0x0418, 0x0418, 0x041a, 0x041b, 0x041c, 0x041d, 0x041e, 0x041f, 0x0420, 0x0421, 0x0422, 0x0423, 0x0424, 0x0425, 0x0426, 0x0427, 0x0428, 0x0429, 0x042a, 0x042b, 0x042c, 0x042d, 0x042e, 0x042f, 0x0430, 0x0431, 0x0432, 0x0433, 0x0434, 0x0435, 0x0436, 0x0437, 0x0438, 0x0438, 0x043a, 0x043b, 0x043c, 0x043d, 0x043e, 0x043f, 0x0440, 0x0441, 0x0442, 0x0443, 0x0444, 0x0445, 0x0446, 0x0447, 0x0448, 0x0449, 0x044a, 0x044b, 0x044c, 0x044d, 0x044e, 0x044f, 0x0435, 0x0435, 0x0452, 0x0433, 0x0454, 0x0455, 0x0456, 0x0456, 0x0458, 0x0459, 0x045a, 0x045b, 0x043a, 0x0438, 0x0443, 0x045f, 0x0460, 0x0461, 0x0462, 0x0463, 0x0464, 0x0465, 0x0466, 0x0467, 0x0468, 0x0469, 0x046a, 0x046b, 0x046c, 0x046d, 0x046e, 0x046f, 0x0470, 0x0471, 0x0472, 0x0473, 0x0474, 0x0475, 0x0474, 0x0475, 0x0478, 0x0479, 0x047a, 0x047b, 0x047c, 0x047d, 0x047e, 0x047f, 0x0480, 0x0481, 0x0482, 0x0483, 0x0484, 0x0485, 0x0486, 0x0487, 0x0488, 0x0489, 0x048a, 0x048b, 0x048c, 0x048d, 0x048e, 0x048f, 0x0490, 0x0491, 0x0492, 0x0493, 0x0494, 0x0495, 0x0496, 0x0497, 0x0498, 0x0499, 0x049a, 0x049b, 0x049c, 0x049d, 0x049e, 0x049f, 0x04a0, 0x04a1, 0x04a2, 0x04a3, 0x04a4, 0x04a5, 0x04a6, 0x04a7, 0x04a8, 0x04a9, 0x04aa, 0x04ab, 0x04ac, 0x04ad, 0x04ae, 0x04af, 0x04b0, 0x04b1, 0x04b2, 0x04b3, 0x04b4, 0x04b5, 0x04b6, 0x04b7, 0x04b8, 0x04b9, 0x04ba, 0x04bb, 0x04bc, 0x04bd, 0x04be, 0x04bf, 0x04c0, 0x0416, 0x0436, 0x04c3, 0x04c4, 0x04c5, 0x04c6, 0x04c7, 0x04c8, 0x04c9, 0x04ca, 0x04cb, 0x04cc, 0x04cd, 0x04ce, 0x04cf, 0x0410, 0x0430, 0x0410, 0x0430, 0x04d4, 0x04d5, 0x0415, 0x0435, 0x04d8, 0x04d9, 0x04d8, 0x04d9, 0x0416, 0x0436, 0x0417, 0x0437, 0x04e0, 0x04e1, 0x0418, 0x0438, 0x0418, 0x0438, 0x041e, 0x043e, 0x04e8, 0x04e9, 0x04e8, 0x04e9, 0x042d, 0x044d, 0x0423, 0x0443, 0x0423, 0x0443, 0x0423, 0x0443, 0x0427, 0x0447, 0x04f6, 0x04f7, 0x042b, 0x044b, 0x04fa, 0x04fb, 0x04fc, 0x04fd, 0x04fe, 0x04ff,};
    protected final Context mContext;
    private final int mMaxWordsToRead;

    private NodeArray mRoots;
    private int mMaxDepth;
    private int mInputLength;
    private ContentObserver mObserver = null;
    private char[] mWordBuilder = new char[MAX_WORD_LENGTH];

    protected BTreeDictionary(String dictionaryName, Context context) {
        super(dictionaryName);
        mMaxWordsToRead = context.getResources().getInteger(R.integer.maximum_dictionary_words_to_load);
        mContext = context;
        //creating the root node.
        clearDictionary();
    }

    protected static char toLowerCase(char c) {
        if (c < BASE_CHARS.length) {
            c = BASE_CHARS[c];
        }
        c = Character.toLowerCase(c);
        return c;
    }

    @Override
    protected void loadAllResources() {
        WordReadListener listener = new WordReadListener() {
            private int mReadWords = 0;

            @Override
            public boolean onWordRead(String word, int frequency) {
                if (!TextUtils.isEmpty(word) && frequency > 0) {
                    //adding only good words
                    addWordFromStorageToMemory(word, frequency);
                }
                return ++mReadWords < mMaxWordsToRead && !isClosed();
            }
        };
        readWordsFromActualStorage(listener);

        if (!isClosed()) {
            if (mObserver == null) {
                mObserver = AnyApplication.getFrankenRobot().embody(new DictionaryContentObserver.DictionaryContentObserverDiagram(this));
                registerObserver(mObserver, mContext.getContentResolver());
            }
        }
    }

    protected abstract void readWordsFromActualStorage(WordReadListener wordReadListener);

    /**
     * Adds a word to the dictionary and makes it persistent.
     *
     * @param word      the word to add. If the word is capitalized, then the
     *                  dictionary will recognize it as a capitalized word when
     *                  searched.
     * @param frequency the frequency of occurrence of the word. A frequency of 255 is
     *                  considered the highest.
     */
    public boolean addWord(String word, int frequency) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                Logger.d(TAG, "Dictionary (type " + this.getClass().getName() + ") " + this.getDictionaryName() + " is closed! Can not add word.");
                return false;
            }
            // Safeguard against adding long words. Can cause stack overflow.
            if (word.length() >= getMaxWordLength()) return false;

            Logger.i(TAG, "Adding word '" + word + "' to dictionary (in " + getClass().getSimpleName() + ") with frequency " + frequency);
            //first deleting the word, so it wont conflict in the adding (_ID is unique).
            deleteWord(word);
            //add word to in-memory structure
            addWordRec(mRoots, word, 0, frequency);
            //add word to storage
            addWordToStorage(word, frequency);
        }
        return true;
    }

    protected int getMaxWordLength() {
        return MAX_WORD_LENGTH;
    }

    protected void onStorageChanged() {
        if (isClosed()) return;
        clearDictionary();
        DictionaryASyncLoader.executeLoaderParallel(null, this);
    }

    @Override
    public final void deleteWord(String word) {
        synchronized (mResourceMonitor) {
            if (isClosed()) {
                Logger.d(TAG, "Dictionary (type " + this.getClass().getName() + ") " + this.getDictionaryName() + " is closed! Can not delete word.");
                return;
            }
            deleteWordRec(mRoots, word, 0, word.length());
            deleteWordFromStorage(word);
        }
    }

    private boolean deleteWordRec(final NodeArray children, final CharSequence word, final int offset, final int length) {
        final int count = children.length;
        final char currentChar = word.charAt(offset);
        for (int j = 0; j < count; j++) {
            final Node node = children.data[j];
            if (node.code == currentChar) {
                if (offset == length - 1) {//last character in the word to delete
                    //we need to delete this node. But only if it terminal
                    if (node.terminal) {
                        if (node.children == null || node.children.length == 0) {
                            //terminal node, with no children - can be safely removed
                            children.deleteNode(j);
                        } else {
                            //terminal node with children. So, it is no longer terminal
                            node.terminal = false;
                        }
                        //let's tell that we deleted a node
                        return true;
                    } else {
                        //it is not terminal, and the word to delete is longer
                        //let's tell that we didn't delete
                        return false;
                    }
                } else if (node.terminal &&//a terminal node
                        (node.children == null || node.children.length == 0)) {//has no children
                    //this is not the last character, but this is a terminal node with no children! Nothing to delete here.
                    return false;
                } else {
                    //not the last character in the word to delete, and not a terminal node.
                    //but if the node forward was deleted, then this one might also need to be deleted.
                    final boolean aChildNodeWasDeleted = deleteWordRec(node.children, word, offset + 1, length);
                    if (aChildNodeWasDeleted) {//something was deleted in my children
                        if (node.children.length == 0 && !node.terminal) {
                            //this node just deleted its last child, and it is not a terminal character.
                            //it is not necessary anymore.
                            children.deleteNode(j);
                            //let's tell that we deleted.
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;//nothing to delete here, move along.
    }

    protected abstract void deleteWordFromStorage(String word);

    protected abstract void registerObserver(ContentObserver dictionaryContentObserver, ContentResolver contentResolver);

    protected abstract void addWordToStorage(String word, int frequency);

    @Override
    public void getWords(final KeyCodesProvider codes, final Dictionary.WordCallback callback) {
        if (isLoading() || isClosed()) return;
        mInputLength = codes.length();
        mMaxDepth = mInputLength * 2;
        getWordsRec(mRoots, codes, mWordBuilder, 0, false, 1.0f, 0, callback);
    }

    @Override
    public boolean isValidWord(CharSequence word) {
        return getWordFrequency(word) > 0;
    }


    /**
     * Checks for the given word's frequency.
     *
     * @param word the word to search for. The search should be case-insensitive.
     * @return frequency value (higher is better. 0 means not exists, 1 is minimum, 255 is maximum).
     */
    public final int getWordFrequency(CharSequence word) {
        if (isLoading() || isClosed()) return 0;
        return getWordFrequencyRec(mRoots, word, 0, word.length());
    }

    private int getWordFrequencyRec(final NodeArray children, final CharSequence word, final int offset, final int length) {
        final int count = children.length;
        char currentChar = word.charAt(offset);
        for (int j = 0; j < count; j++) {
            final Node node = children.data[j];
            if (node.code == currentChar) {
                if (offset == length - 1) {
                    if (node.terminal) {
                        return node.frequency;
                    }
                } else {
                    if (node.children != null) {
                        int frequency = getWordFrequencyRec(node.children, word, offset + 1, length);
                        if (frequency > 0)
                            return frequency;
                    }
                }
            }
        }
        //no luck, can't find the word
        return 0;
    }

    /**
     * Recursively traverse the tree for words that match the input. Input
     * consists of a list of arrays. Each item in the list is one input
     * character position. An input character is actually an array of multiple
     * possible candidates. This function is not optimized for speed, assuming
     * that the user dictionary will only be a few hundred words in size.
     *
     * @param roots      node whose children have to be search for matches
     * @param codes      the input character mCodes
     * @param word       the word being composed as a possible match
     * @param depth      the depth of traversal - the length of the word being composed
     *                   thus far
     * @param completion whether the traversal is now in completion mode - meaning that
     *                   we've exhausted the input and we're looking for all possible
     *                   suffixes.
     * @param snr        current weight of the word being formed
     * @param inputIndex position in the input characters. This can be off from the
     *                   depth in case we skip over some punctuations such as
     *                   apostrophe in the traversal. That is, if you type "wouldve",
     *                   it could be matching "would've", so the depth will be one more
     *                   than the inputIndex
     * @param callback   the callback class for adding a word
     */
    private void getWordsRec(NodeArray roots, final KeyCodesProvider codes, final char[] word, final int depth, boolean completion, float snr, int inputIndex, WordCallback callback) {
        final int count = roots.length;
        final int codeSize = mInputLength;
        // Optimization: Prune out words that are too long compared to how much
        // was typed.
        if (depth > mMaxDepth) {
            return;
        }
        int[] currentChars = null;
        if (codeSize <= inputIndex) {
            completion = true;
        } else {
            currentChars = codes.getCodesAt(inputIndex);
        }

        for (int i = 0; i < count; i++) {
            final Node node = roots.data[i];
            final char c = node.code;
            final char lowerC = toLowerCase(c);
            boolean terminal = node.terminal;
            NodeArray children = node.children;
            int freq = node.frequency;
            if (completion) {
                word[depth] = c;
                if (terminal) {
                    if (!callback.addWord(word, 0, depth + 1, (int) (freq * snr), this)) {
                        return;
                    }
                }
                if (children != null) {
                    getWordsRec(children, codes, word, depth + 1, completion, snr, inputIndex, callback);
                }
            } else if (c == QUOTE && currentChars[0] != QUOTE) {
                // Skip the ' and continue deeper
                word[depth] = QUOTE;
                if (children != null) {
                    getWordsRec(children, codes, word, depth + 1, completion, snr, inputIndex, callback);
                }
            } else {
                for (int j = 0; j < currentChars.length; j++) {
                    float addedAttenuation = (j > 0 ? 1f : 3f);
                    if (currentChars[j] == -1) {
                        break;
                    }
                    if (currentChars[j] == lowerC || currentChars[j] == c) {
                        word[depth] = c;

                        if (codes.length() == depth + 1) {
                            if (terminal) {
                                if (INCLUDE_TYPED_WORD_IF_VALID || !same(word, depth + 1, codes.getTypedWord())) {
                                    callback.addWord(word, 0, depth + 1, (int) (freq * snr * addedAttenuation * FULL_WORD_FREQ_MULTIPLIER), this);
                                }
                            }
                            if (children != null) {
                                getWordsRec(children, codes, word, depth + 1, true, snr * addedAttenuation, inputIndex + 1, callback);
                            }
                        } else if (children != null) {
                            getWordsRec(children, codes, word, depth + 1, false, snr * addedAttenuation, inputIndex + 1, callback);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected final void closeAllResources() {
        clearDictionary();
        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }

        closeStorage();
    }

    protected void addWordFromStorageToMemory(String word, int frequency) {
        addWordRec(mRoots, word, 0, frequency);
    }

    private void addWordRec(NodeArray children, final String word, final int depth, final int frequency) {
        final int wordLength = word.length();
        final char c = word.charAt(depth);
        // Does children have the current character?
        final int childrenLength = children.length;
        Node childNode = null;
        boolean found = false;
        for (int i = 0; i < childrenLength; i++) {
            childNode = children.data[i];
            if (childNode.code == c) {
                found = true;
                break;
            }
        }
        if (!found) {
            childNode = new Node();
            childNode.code = c;
            children.add(childNode);
        }
        if (wordLength == depth + 1) {
            // Terminate this word
            childNode.terminal = true;
            childNode.frequency = frequency;
            // words
            return;
        }
        if (childNode.children == null) {
            childNode.children = new NodeArray();
        }
        addWordRec(childNode.children, word, depth + 1, frequency);
    }

    private void clearDictionary() {
        mRoots = new NodeArray(INITIAL_ROOT_CAPACITY);
    }

    protected abstract void closeStorage();

    static class Node {
        char code;
        int frequency;
        boolean terminal;
        NodeArray children;
    }

    static class NodeArray {
        private static final int INCREMENT = 2;
        Node[] data;
        int length = 0;

        NodeArray(int initialCapacity) {
            data = new Node[initialCapacity];
        }

        NodeArray() {
            this(INCREMENT);
        }

        void add(Node n) {
            length++;
            if (length > data.length) {
                Node[] tempData = new Node[length + INCREMENT];
                System.arraycopy(data, 0, tempData, 0, data.length);
                data = tempData;
            }
            data[length - 1] = n;
        }

        public void deleteNode(int nodeIndexToDelete) {
            length--;
            if (length > 0) {
                for (int i = nodeIndexToDelete; i < length; i++) {
                    data[i] = data[i + 1];
                }
            }
        }
    }
}
