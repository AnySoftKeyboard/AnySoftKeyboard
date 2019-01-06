package net.evendanan.pixel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SettingsTileViewTest {

    @Test
    public void testPortraitLayout() {
        SettingsTileView view = buildSettingTileView(Configuration.ORIENTATION_PORTRAIT);

        Assert.assertEquals(LinearLayout.HORIZONTAL, view.getOrientation());
        final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, layoutParams.width);
        Assert.assertEquals(LinearLayout.LayoutParams.WRAP_CONTENT, layoutParams.height);
    }

    @Test
    public void testLandscapeLayout() {
        SettingsTileView view = buildSettingTileView(Configuration.ORIENTATION_LANDSCAPE);

        Assert.assertEquals(LinearLayout.VERTICAL, view.getOrientation());
        final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, layoutParams.height);
        Assert.assertEquals(1f, layoutParams.weight, 0f);
        Assert.assertEquals(0, layoutParams.width);

    }

    @Test
    public void testUndefineOrientationLayout() {
        //same as portrait
        testPortraitLayout();
    }

    @Test
    public void testLabelSetterGetter() {
        SettingsTileView view = buildSettingTileView();

        view.setLabel("test 1 2 3");
        Assert.assertEquals("test 1 2 3", view.getLabel().toString());

        TextView innerTextView = view.findViewById(R.id.tile_label);
        Assert.assertNotNull(innerTextView);
        Assert.assertSame(view.getLabel(), innerTextView.getText());
    }

    @Test
    public void testImageSetterGetter() {
        SettingsTileView view = buildSettingTileView();

        view.setImage(android.R.drawable.arrow_up_float);
        Assert.assertEquals(android.R.drawable.arrow_up_float, Shadows.shadowOf(view.getImage()).getCreatedFromResId());

        ImageView innerImageView = view.findViewById(R.id.tile_image);
        Assert.assertNotNull(innerImageView);
        Assert.assertSame(view.getImage(), innerImageView.getDrawable());
    }

    @Test
    public void testInitialLayoutAttrValues() {
        SettingsTileView view = buildSettingTileView();

        Assert.assertEquals(android.R.drawable.ic_delete, Shadows.shadowOf(view.getImage()).getCreatedFromResId());
        Assert.assertEquals(getApplicationContext().getText(android.R.string.paste).toString(), view.getLabel().toString());
    }

    private SettingsTileView buildSettingTileView() {
        return buildSettingTileView(Configuration.ORIENTATION_PORTRAIT);
    }

    private SettingsTileView buildSettingTileView(int orientation) {
        ActivityController<FragmentActivity> controller = Robolectric.buildActivity(FragmentActivity.class);
        controller.get().getResources().getConfiguration().orientation = orientation;
        controller.setup();
        final SettingsTileView view = (SettingsTileView) LayoutInflater.from(controller.get()).inflate(R.layout.settings_tile_view_test_layout, null);
        Robolectric.flushForegroundThreadScheduler();
        return view;
    }
}