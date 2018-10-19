package com.anysoftkeyboard.addons;

import static com.menny.android.anysoftkeyboard.R.styleable.KeyboardLayout;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SupportTest {

    @Test
    public void testSamePackageSameValues() {
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable = Support.createBackwardCompatibleStyleable(KeyboardLayout,
                getApplicationContext(),
                getApplicationContext(),
                sparseIntArray);

        Assert.assertSame(backwardCompatibleStyleable, R.styleable.KeyboardLayout);
        Assert.assertEquals(backwardCompatibleStyleable.length, sparseIntArray.size());
        for (int attrId : backwardCompatibleStyleable) {
            Assert.assertEquals(attrId, sparseIntArray.get(attrId));
        }
    }

    @Test
    public void testDifferentPackageDifferentValues() {
        //this is a long setup
        Context remoteContext = Mockito.mock(Context.class);
        Mockito.doReturn("com.some.other.package").when(remoteContext).getPackageName();
        Resources remoteRes = Mockito.mock(Resources.class);
        Mockito.doReturn(123).when(remoteRes).getIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(remoteRes).when(remoteContext).getResources();

        //starting test
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable = Support.createBackwardCompatibleStyleable(KeyboardLayout,
                getApplicationContext(),
                remoteContext,
                sparseIntArray);

        Mockito.verify(remoteRes).getIdentifier("showPreview", "attr", "com.some.other.package");
        Mockito.verifyNoMoreInteractions(remoteRes);

        Assert.assertNotSame(backwardCompatibleStyleable, R.styleable.KeyboardLayout);
        Assert.assertEquals(backwardCompatibleStyleable.length, R.styleable.KeyboardLayout.length);
        Assert.assertEquals(backwardCompatibleStyleable.length, sparseIntArray.size());
        for (int attrId : backwardCompatibleStyleable) {
            if (attrId == 123) {
                Assert.assertEquals(R.attr.showPreview, sparseIntArray.get(123));
            } else {
                Assert.assertEquals(attrId, sparseIntArray.get(attrId));
            }
        }
    }

    @Test
    public void testDifferentPackageNoValue() {
        //this is a long setup
        Context remoteContext = Mockito.mock(Context.class);
        Mockito.doReturn("com.some.other.package").when(remoteContext).getPackageName();
        Resources remoteRes = Mockito.mock(Resources.class);
        Mockito.doReturn(0).when(remoteRes).getIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(remoteRes).when(remoteContext).getResources();

        //starting test
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable = Support.createBackwardCompatibleStyleable(KeyboardLayout,
                getApplicationContext(),
                remoteContext,
                sparseIntArray);

        Mockito.verify(remoteRes).getIdentifier("showPreview", "attr", "com.some.other.package");
        Mockito.verifyNoMoreInteractions(remoteRes);

        Assert.assertNotSame(backwardCompatibleStyleable, R.styleable.KeyboardLayout);
        Assert.assertEquals(backwardCompatibleStyleable.length, R.styleable.KeyboardLayout.length - 1);
        Assert.assertEquals(backwardCompatibleStyleable.length, sparseIntArray.size());
        for (int attrId : backwardCompatibleStyleable) {
            Assert.assertEquals(attrId, sparseIntArray.get(attrId));
        }
    }

    public static void ensureKeyboardAtIndexEnabled(int keyboardIndex, boolean enabled) {
        ensureAddOnAtIndexEnabled(AnyApplication.getKeyboardFactory(getApplicationContext()), keyboardIndex, enabled);
    }

    public static void ensureAddOnAtIndexEnabled(AddOnsFactory<? extends AddOn> factory, int index, boolean enabled) {
        final AddOn addOn = factory.getAllAddOns().get(index);
        factory.setAddOnEnabled(addOn.getId(), enabled);
    }
}