package com.anysoftkeyboard.ui.tutorials;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChangeLogFragmentShowLatestTest
        extends RobolectricFragmentTestCase<ChangeLogFragment> {
    @NonNull
    @Override
    protected ChangeLogFragment createFragment() {
        return new ChangeLogFragment.LatestChangeLogFragment();
    }

    @Test
    public void testRootViewHasLatestLog() {
        ViewGroup rootView = startFragment().getView().findViewById(R.id.card_with_read_more);
        Assert.assertTrue(rootView.getChildAt(0) instanceof LinearLayout);
        LinearLayout container = (LinearLayout) rootView.getChildAt(0);

        int headersFound = 0;
        int changeLogItems = 0;
        int linkItems = 0;
        int visibleLinkItems = 0;
        for (int childViewIndex = 0; childViewIndex < container.getChildCount(); childViewIndex++) {
            final View childView = container.getChildAt(childViewIndex);
            final int id = childView.getId();
            if (id == R.id.changelog_version_title) {
                headersFound++;
            } else if (id == R.id.chang_log_item) {
                changeLogItems++;
            } else if (id == R.id.change_log__web_link_item) {
                linkItems++;
                if (childView.getVisibility() != View.GONE) visibleLinkItems++;
            }
        }

        Assert.assertEquals(1, headersFound);
        Assert.assertEquals(1, changeLogItems);
        Assert.assertEquals(1, linkItems);
        Assert.assertEquals(0, visibleLinkItems);
    }

    @Test
    public void testChangeLogDoesNotHaveLinkToOpenWebChangeLog() {
        LinearLayout rootView = startFragment().getView().findViewById(R.id.card_with_read_more);
        Assert.assertEquals(
                View.GONE, rootView.findViewById(R.id.change_log__web_link_item).getVisibility());
    }
}
