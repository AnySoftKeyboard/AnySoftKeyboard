package com.anysoftkeyboard.addons.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AddOnStoreSearchViewTest {

  @Test
  public void testSetsTheTitleFromAttribute() {
    Context context = ApplicationProvider.getApplicationContext();
    final var rootTest = LayoutInflater.from(context).inflate(R.layout.test_search_layout, null);

    final AddOnStoreSearchView underTest = rootTest.findViewById(R.id.test_search_view);
    final TextView title = underTest.findViewById(R.id.cta_title);
    Assert.assertEquals("Search for add-ons", title.getText().toString());

    underTest.setTitle("now this");
    Assert.assertEquals("now this", title.getText().toString());
  }

  @Test
  public void testSearchHappyPath() {
    Application context = ApplicationProvider.getApplicationContext();
    ShadowApplication shadowApplication = Shadows.shadowOf(context);

    final var rootTest = LayoutInflater.from(context).inflate(R.layout.test_search_layout, null);

    final AddOnStoreSearchView underTest = rootTest.findViewById(R.id.test_search_view);
    underTest.setTag("An addon");
    final var noMarketError = underTest.findViewById(R.id.no_store_found_error);
    Assert.assertEquals(View.GONE, noMarketError.getVisibility());

    var viewShadow = Shadows.shadowOf(underTest);
    viewShadow.getOnClickListener().onClick(underTest);

    Assert.assertEquals(View.GONE, noMarketError.getVisibility());

    var intent = shadowApplication.getNextStartedActivity();
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(
        Uri.parse("market://search?q=AnySoftKeyboard%20An%20addon"), intent.getData());
  }

  @Test
  public void testUtilityStart() {
    Application context = ApplicationProvider.getApplicationContext();
    Application spy = Mockito.spy(context);
    Mockito.doThrow(new RuntimeException()).when(spy).startActivity(Mockito.any());
    Assert.assertFalse(AddOnStoreSearchView.startMarketActivity(spy, "play"));
  }

  @Test
  public void testUtilityNoMarketError() {
    Application context = ApplicationProvider.getApplicationContext();
    Assert.assertTrue(AddOnStoreSearchView.startMarketActivity(context, "play"));

    var intent = Shadows.shadowOf(context).getNextStartedActivity();
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(Uri.parse("market://search?q=AnySoftKeyboard%20play"), intent.getData());
  }
}
