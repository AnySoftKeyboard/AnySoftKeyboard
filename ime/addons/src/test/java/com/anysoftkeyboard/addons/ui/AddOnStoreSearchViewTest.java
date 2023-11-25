package com.anysoftkeyboard.addons.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;

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

  public void testControllerClick() {
    AddOnStoreSearchController mock = Mockito.mock(AddOnStoreSearchController.class);
    Context context = ApplicationProvider.getApplicationContext();
    final var rootTest = LayoutInflater.from(context).inflate(R.layout.test_search_layout, null);

    final AddOnStoreSearchView underTest = rootTest.findViewById(R.id.test_search_view);

    underTest.setSearchController(mock);
    Shadows.shadowOf(underTest).getOnClickListener().onClick(underTest);
    Mockito.verify(mock).searchForAddOns();

    underTest.setSearchController(null);
    Assert.assertNull(Shadows.shadowOf(underTest).getOnClickListener());
  }
}
