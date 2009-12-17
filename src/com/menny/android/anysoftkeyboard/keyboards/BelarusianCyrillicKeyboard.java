package com.menny.android.anysoftkeyboard.keyboards;

import android.view.KeyEvent;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;

/**
 * @author Alex Buloichik(alex73mail@gmail.com)
 */
public class BelarusianCyrillicKeyboard extends AnyKeyboard implements
        HardKeyboardTranslator {
    private static final HardKeyboardSequenceHandler msKeySequenceHandler;

    static {
        msKeySequenceHandler = new HardKeyboardSequenceHandler();

        msKeySequenceHandler
                .addQwertyTranslation("\u0439\u0446\u0443\u043A\u0435\u043D\u0433\u0448\u045E\u0437"
                        + "\u0444\u044B\u0432\u0430\u043F\u0440\u043E\u043B\u0434"
                        + "\u044F\u0447\u0441\u043C\u0456\u0442\u044C");

        msKeySequenceHandler.addSequence(new int[] { KeyEvent.KEYCODE_U,
                KeyEvent.KEYCODE_U }, (char) 0x0491);
    }

    public BelarusianCyrillicKeyboard(AnyKeyboardContextProvider context) {
        super(context, R.xml.be_cyrillic);
    }
    
    @Override
    protected void setPopupKeyChars(Key aKey) {
        if ((aKey.codes != null) && (aKey.codes.length > 0)) {
            switch ((char) aKey.codes[0]) {
            case '\u0433':
                aKey.popupCharacters = "\u0433\u0491";
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
                    (char) action.getKeyCode(), getKeyboardContext());
            if (translated != 0) {
                if (action.isShiftActive())
                    translated = Character.toUpperCase(translated);
                action.setNewKeyCode(translated);
            }
        }
    }
}
