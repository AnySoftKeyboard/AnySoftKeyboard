package com.menny.android.anysoftkeyboard.keyboards;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

/**
 * @author Alex Buloichik(alex73mail@gmail.com)
 */
public class BelarusianLatinKeyboard extends InternalAnyKeyboard implements
        HardKeyboardTranslator {
    private static final HardKeyboardSequenceHandler msKeySequenceHandler;

    static {
        msKeySequenceHandler = new HardKeyboardSequenceHandler();        
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_C,
                KeyEvent.KEYCODE_C }, (char) 0x0107);        
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_C,
                KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_C }, (char) 0x010D);
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_L,
                KeyEvent.KEYCODE_L }, (char) 0x0142);        
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_N,
                KeyEvent.KEYCODE_N }, (char) 0x0144);
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_S,
                KeyEvent.KEYCODE_S }, (char) 0x015B);        
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_S,
                KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_S}, (char) 0x0161);
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_U,
                KeyEvent.KEYCODE_U }, (char) 0x016D);
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_Z,
                KeyEvent.KEYCODE_Z }, (char) 0x017A);        
        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_Z,
                KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_Z}, (char) 0x017E);
    }

    public BelarusianLatinKeyboard(AnyKeyboardContextProvider context) {
        super(context, R.xml.be_latin);
    }

    @Override
    protected void setPopupKeyChars(Key aKey) {
        if ((aKey.codes != null) && (aKey.codes.length > 0)) {
            switch ((char) aKey.codes[0]) {
            case 'c':
                aKey.popupCharacters = "c\u0107\u010D";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            case 'l':
                aKey.popupCharacters = "l\u0142";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            case 'n':
                aKey.popupCharacters = "n\u0144";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            case 's':
                aKey.popupCharacters = "s\u015B\u0161";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            case 'u':
                aKey.popupCharacters = "u\u016D";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            case 'z':
                aKey.popupCharacters = "z\u017A\u017E";
                aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
                break;
            default:
                super.setPopupKeyChars(aKey);
            }
        }
    }

    public void translatePhysicalCharacter(HardKeyboardAction action) {
        if (action.isAltActive())
            return;
        else {
            char translated = msKeySequenceHandler.getSequenceCharacter(
                    (char) action.getKeyCode(), getASKContext());
            if (translated != 0) {
                if (action.isShiftActive())
                    translated = Character.toUpperCase(translated);
                action.setNewKeyCode(translated);
            }
        }
    }
}
