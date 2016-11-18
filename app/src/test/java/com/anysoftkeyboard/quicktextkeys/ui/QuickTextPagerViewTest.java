package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.LayoutInflater;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

@RunWith(RobolectricTestRunner.class)
public class QuickTextPagerViewTest {

    private QuickTextPagerView mUnderTest;

    @Before
    public void setup() {
        mUnderTest = (QuickTextPagerView) LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.quick_text_popup_root_view, null, false);
    }

    @Test
    public void setOnKeyboardActionListener() throws Exception {
        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        ShadowView shadowView = Shadows.shadowOf(mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings));

        Assert.assertNull(shadowView.getOnClickListener());

        mUnderTest.setOnKeyboardActionListener(listener);

        Assert.assertNotNull(shadowView.getOnClickListener());
    }

}