//package com.menny.android.anysoftkeyboard.keyboards;
//
//import android.view.KeyEvent;
//
//import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
//import com.menny.android.anysoftkeyboard.R;
//import com.menny.android.anysoftkeyboard.Dictionary.Dictionary;
//import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;
//import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardAction;
//import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
//
///**
// * @author Alex Buloichik(alex73mail@gmail.com)
// */
//public class BelarusianLatinKeyboard extends AnyKeyboard implements
//        HardKeyboardTranslator {
//    private static final HardKeyboardSequenceHandler msKeySequenceHandler;
//    
//    static {
//        msKeySequenceHandler = new HardKeyboardSequenceHandler();
//    }
//
//    public BelarusianLatinKeyboard(AnyKeyboardContextProvider context) {
//        super(context, KeyboardFactory.BE_LATIN_KEYBOARD, R.xml.be_latin,
//                false, R.string.be_latin_keyboard, false,
//                Dictionary.Language.None);
//    }
//
//    @Override
//    public int getKeyboardIcon() {
//        return R.drawable.be_latin;
//    }
//
//    public void translatePhysicalCharacter(HardKeyboardAction action) {
//        return;
//    }
//
//    @Override
//    public Language getDefaultDictionaryLanguage() {
//        return Language.None;
//    }
//}
