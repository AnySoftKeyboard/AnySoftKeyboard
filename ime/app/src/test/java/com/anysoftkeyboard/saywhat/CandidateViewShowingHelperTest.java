package com.anysoftkeyboard.saywhat;

import android.view.View;
import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class CandidateViewShowingHelperTest {

  @Test
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void testHappyPath() {
    CandidateViewShowingHelper helper = new CandidateViewShowingHelper();

    final PublicNotices ime = Mockito.mock(PublicNotices.class);
    final KeyboardViewContainerView container = Mockito.mock(KeyboardViewContainerView.class);
    Mockito.doReturn(container).when(ime).getInputViewContainer();
    Mockito.doReturn(null).when(container).getCandidateView();

    Assert.assertFalse(helper.shouldShow(ime));

    CandidateView candidate = Mockito.mock(CandidateView.class);
    Mockito.doReturn(candidate).when(container).getCandidateView();
    Mockito.doReturn(View.GONE).when(candidate).getVisibility();
    Assert.assertFalse(helper.shouldShow(ime));

    Mockito.doReturn(View.INVISIBLE).when(candidate).getVisibility();
    Assert.assertFalse(helper.shouldShow(ime));

    Mockito.doReturn(View.VISIBLE).when(candidate).getVisibility();
    Assert.assertTrue(helper.shouldShow(ime));
  }
}
