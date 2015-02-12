package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.KeyEvent;
import android.view.View;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.menny.android.anysoftkeyboard.R;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FrameKeyboardViewClickListenerTest {

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

	public void testOnClickBackSpace() throws Exception {
		OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
		FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
		Mockito.verifyZeroInteractions(keyboardActionListener);
		View view = new View(Robolectric.application);
		view.setId(R.id.quick_keys_popup_backspace);
		listener.onClick(view);
		Mockito.verify(keyboardActionListener).onKey(KeyEvent.KEYCODE_DEL, null, 0, null, true);
		Mockito.verifyNoMoreInteractions(keyboardActionListener);
	}

	public void testOnClickEnter() throws Exception {
		OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
		FrameKeyboardViewClickListener listener = new FrameKeyboardViewClickListener(keyboardActionListener);
		Mockito.verifyZeroInteractions(keyboardActionListener);
		View view = new View(Robolectric.application);
		view.setId(R.id.quick_keys_popup_return);
		listener.onClick(view);
		Mockito.verify(keyboardActionListener).onKey(KeyEvent.KEYCODE_ENTER, null, 0, null, true);
		Mockito.verifyNoMoreInteractions(keyboardActionListener);
	}
}