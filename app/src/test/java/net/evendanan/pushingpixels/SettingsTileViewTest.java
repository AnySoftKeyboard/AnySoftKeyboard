package net.evendanan.pushingpixels;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;

@RunWith(AnySoftKeyboardTestRunner.class)
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
        Assert.assertEquals("test 1 2 3", view.getLabel());

        TextView innerTextView = view.findViewById(R.id.tile_label);
        Assert.assertNotNull(innerTextView);
        Assert.assertSame(view.getLabel(), innerTextView.getText());
    }

    @Test
    public void testImageSetterGetter() {
        SettingsTileView view = buildSettingTileView();

        view.setImage(R.drawable.ic_app_shortcut_home);
        Assert.assertEquals(R.drawable.ic_app_shortcut_home, Shadows.shadowOf(view.getImage()).getCreatedFromResId());

        ImageView innerImageView = view.findViewById(R.id.tile_image);
        Assert.assertNotNull(innerImageView);
        Assert.assertSame(view.getImage(), innerImageView.getDrawable());
    }

    @Test
    public void testInitialLayoutAttrValues() {
        SettingsTileView view = buildSettingTileView();

        Assert.assertEquals(R.drawable.ic_language_root_keyboards, Shadows.shadowOf(view.getImage()).getCreatedFromResId());
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.language_root_tile), view.getLabel());
    }

    private SettingsTileView buildSettingTileView() {
        return buildSettingTileView(Configuration.ORIENTATION_PORTRAIT);
    }

    private SettingsTileView buildSettingTileView(int orientation) {
        ActivityController<MainSettingsActivity> controller = Robolectric.buildActivity(MainSettingsActivity.class);
        controller.get().getResources().getConfiguration().orientation = orientation;
        controller.setup();
        final SettingsTileView view = (SettingsTileView) LayoutInflater.from(controller.get()).inflate(R.layout.settings_tile_view_test_layout, null);
        Robolectric.flushForegroundThreadScheduler();
        return view;
    }
}