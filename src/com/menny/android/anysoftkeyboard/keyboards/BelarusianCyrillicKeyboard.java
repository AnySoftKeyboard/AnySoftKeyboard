//package com.menny.android.anysoftkeyboard.keyboards;
//
//import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
//import com.menny.android.anysoftkeyboard.R;
//import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
//import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;
//import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
//
///**
// * @author Alex Buloichik(alex73mail@gmail.com)
// */
//public class BelarusianCyrillicKeyboard extends AnyKeyboard implements
//        HardKeyboardTranslator {
//    private static final HardKeyboardSequenceHandler msKeySequenceHandler;
//
//    static {
//        msKeySequenceHandler = new HardKeyboardSequenceHandler();
//    }
//
//    public BelarusianCyrillicKeyboard(AnyKeyboardContextProvider context) {
//        super(context, KeyboardFactory.BE_CYRILLIC_KEYBOARD, R.xml.be_cyrillic,
//                false, R.string.be_cyrillic_keyboard, false,
//                Dictionary.Language.None);
//    }
//
//    @Override
//    public int getKeyboardIcon() {
//        return R.drawable.be_cyrillic;
//    }
//
//    public void translatePhysicalCharacter(HardKeyboardAction action) {
//        if (action.isAltActive())
//            return;
//        else {
//            char translated = msKeySequenceHandler.getSequenceCharacter(
//                    (char) action.getKeyCode(), getKeyboardContext());
//            if (translated != 0) {
//                if (action.isShiftActive())
//                    translated = Character.toUpperCase(translated);
//                action.setNewKeyCode(translated);
//            }
//        }
//    }
//
//    @Override
//    public Language getDefaultDictionaryLanguage() {
//        return Language.None;
//    }
//}
