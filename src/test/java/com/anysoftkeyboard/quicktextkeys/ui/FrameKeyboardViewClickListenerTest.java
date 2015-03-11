package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.KeyEvent;
import android.view.View;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.menny.android.anysoftkeyboard.R;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FrameKeyboardViewClickListenerTest {

	@Test
	public void testOnClickClose() throws Exception {
		OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
		FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
		Mockito.verifyZeroInteractions(keyboardActionListener);
		View view = new View(Robolectric.application);
		view.setId(R.id.quick_keys_popup_close);
		listener.onClick(view);
		Mockito.verify(keyboardActionListener).onCancel();
		Mockito.verifyNoMoreInteractions(keyboardActionListener);
	}

	@Test
	public void testOnClickBackSpace() throws Exception {
		OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
		FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
		Mockito.verifyZeroInteractions(keyboardActionListener);
		View view = new View(Robolectric.application);
		view.setId(R.id.quick_keys_popup_backspace);
		listener.onClick(view);
		Mockito.verify(keyboardActionListener).onKey(KeyCodes.DELETE, null, 0, null, true);
		Mockito.verifyNoMoreInteractions(keyboardActionListener);
	}
}