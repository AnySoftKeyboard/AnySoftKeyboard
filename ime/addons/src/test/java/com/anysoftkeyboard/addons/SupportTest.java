package com.anysoftkeyboard.addons;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseIntArray;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SupportTest {

    @Test
    public void testSamePackageSameValues() {
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable =
                Support.createBackwardCompatibleStyleable(
                        R.styleable.KeyboardLayout,
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
        // this is a long setup
        Context remoteContext = Mockito.mock(Context.class);
        Mockito.doReturn("com.some.other.package").when(remoteContext).getPackageName();
        Resources remoteRes = Mockito.mock(Resources.class);
        Mockito.doAnswer(
                        invocation -> {
                            final Object packageName = invocation.getArgument(2);
                            final String resName = invocation.getArgument(0).toString();
                            if (packageName == null || packageName.equals("android")) {
                                return getApplicationContext()
                                        .getResources()
                                        .getIdentifier(resName, invocation.getArgument(1), null);
                            } else {
                                switch (resName) {
                                    case "showPreview":
                                        return 123;
                                    case "autoCap":
                                        return 124;
                                    default:
                                        return 0;
                                }
                            }
                        })
                .when(remoteRes)
                .getIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(remoteRes).when(remoteContext).getResources();

        // starting test
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable =
                Support.createBackwardCompatibleStyleable(
                        R.styleable.KeyboardLayout,
                        getApplicationContext(),
                        remoteContext,
                        sparseIntArray);

        Mockito.verify(remoteRes).getIdentifier("showPreview", "attr", "com.some.other.package");
        Mockito.verify(remoteRes).getIdentifier("autoCap", "attr", "com.some.other.package");
        Mockito.verify(remoteRes).getIdentifier("reverse", "attr", "com.some.other.package");
        Mockito.verifyNoMoreInteractions(remoteRes);

        Assert.assertNotSame(backwardCompatibleStyleable, R.styleable.KeyboardLayout);
        Assert.assertEquals(
                backwardCompatibleStyleable.length, R.styleable.KeyboardLayout.length - 1);
        Assert.assertEquals(backwardCompatibleStyleable.length, sparseIntArray.size());
        for (int attrId : backwardCompatibleStyleable) {
            if (attrId == 123) {
                Assert.assertEquals(R.attr.showPreview, sparseIntArray.get(123));
            } else if (attrId == 124) {
                Assert.assertEquals(R.attr.autoCap, sparseIntArray.get(124));
            } else {
                Assert.assertEquals(attrId, sparseIntArray.get(attrId));
            }
        }
    }

    @Test
    public void testDifferentPackageNoValue() {
        // this is a long setup
        Context remoteContext = Mockito.mock(Context.class);
        Mockito.doReturn("com.some.other.package").when(remoteContext).getPackageName();
        Resources remoteRes = Mockito.mock(Resources.class);
        Mockito.doReturn(0)
                .when(remoteRes)
                .getIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(remoteRes).when(remoteContext).getResources();

        // starting test
        SparseIntArray sparseIntArray = new SparseIntArray();
        int[] backwardCompatibleStyleable =
                Support.createBackwardCompatibleStyleable(
                        R.styleable.KeyboardLayout,
                        getApplicationContext(),
                        remoteContext,
                        sparseIntArray);

        Mockito.verify(remoteRes).getIdentifier("showPreview", "attr", "com.some.other.package");
        Mockito.verify(remoteRes).getIdentifier("autoCap", "attr", "com.some.other.package");
        Mockito.verify(remoteRes).getIdentifier("reverse", "attr", "com.some.other.package");
        Mockito.verifyNoMoreInteractions(remoteRes);

        Assert.assertNotSame(backwardCompatibleStyleable, R.styleable.KeyboardLayout);
        Assert.assertEquals(
                backwardCompatibleStyleable.length, R.styleable.KeyboardLayout.length - 3);
        Assert.assertEquals(backwardCompatibleStyleable.length, sparseIntArray.size());
        for (int attrId : backwardCompatibleStyleable) {
            Assert.assertEquals(attrId, sparseIntArray.get(attrId));
        }
    }
}
