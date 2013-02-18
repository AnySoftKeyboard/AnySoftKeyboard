package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.WordComposer;

@TargetApi(11)
public class DeviceSpecific_V11 extends DeviceSpecific_V8 {
	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V11";
	}
	
	@Override
	public void commitCorrectionToInputConnection(InputConnection ic,
			WordComposer word) {
		super.commitCorrectionToInputConnection(ic, word);
		CorrectionInfo correctionInfo = new CorrectionInfo(
				word.globalCursorPosition() - word.getTypedWord().length(),
				word.getTypedWord(), word.getPreferredWord());

		ic.commitCorrection(correctionInfo);
	}
}
