package com.anysoftkeyboard.quicktextkeys.ui;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CloseKeyboardViewClickListenerTest extends TestCase {

	public void testOnClick() throws Exception {
		OnKeyboardActionListener keyboardActionListener = Mockito.mock(OnKeyboardActionListener.class);
		CloseKeyboardViewClickListener listener = new CloseKeyboardViewClickListener(keyboardActionListener);
		Mockito.verifyZeroInteractions(keyboardActionListener);
		listener.onClick(null);
		Mockito.verify(keyboardActionListener).onCancel();
		Mockito.verifyNoMoreInteractions(keyboardActionListener);
	}
}