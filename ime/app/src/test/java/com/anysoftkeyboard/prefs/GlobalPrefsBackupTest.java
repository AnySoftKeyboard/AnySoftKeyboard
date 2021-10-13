package com.anysoftkeyboard.prefs;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.test.TestUtils;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GlobalPrefsBackupTest {

    public static void assertRootsEqual(PrefsRoot root1, PrefsRoot root2) {
        Assert.assertEquals(root1.getVersion(), root2.getVersion());

        assertPrefItemsEqual(root1, root2);
    }

    public static void assertPrefItemsEqual(PrefItem prefItem1, PrefItem prefItem2) {
        for (Map.Entry<String, String> values : prefItem1.getValues()) {
            Assert.assertEquals(values.getValue(), prefItem2.getValue(values.getKey()));
        }

        for (Map.Entry<String, String> values : prefItem2.getValues()) {
            Assert.assertEquals(values.getValue(), prefItem1.getValue(values.getKey()));
        }

        final List<PrefItem> prefItems1 = TestUtils.convertToList(prefItem1.getChildren());
        final List<PrefItem> prefItems2 = TestUtils.convertToList(prefItem2.getChildren());
        Assert.assertEquals(prefItems1.size(), prefItems2.size());
        for (int childIndex = 0; childIndex < prefItems1.size(); childIndex++) {
            assertPrefItemsEqual(prefItems1.get(childIndex), prefItems2.get(childIndex));
        }
    }

    @Test
    public void testBackupRestoreCustomPath() throws Exception {
        File customFile = new File(Environment.getExternalStorageDirectory() + "/testBackup.xml");
        final FakePrefsProvider fakePrefsProvider = new FakePrefsProvider("id1");
        List<GlobalPrefsBackup.ProviderDetails> fakeDetails =
                Collections.singletonList(
                        new GlobalPrefsBackup.ProviderDetails(
                                fakePrefsProvider, R.string.pop_text_type_title));

        final AtomicReference<List<GlobalPrefsBackup.ProviderDetails>> hits =
                new AtomicReference<>(new ArrayList<>());

        GlobalPrefsBackup.backup(Pair.create(fakeDetails, new Boolean[] {true}), customFile)
                .blockingSubscribe(p -> hits.get().add(p));

        Assert.assertEquals(1, hits.get().size());
        Assert.assertSame(fakePrefsProvider, hits.get().get(0).provider);

        hits.get().clear();

        Assert.assertTrue(customFile.exists());
        Assert.assertTrue(customFile.length() > 0);

        Assert.assertNull(fakePrefsProvider.storedPrefsRoot);

        GlobalPrefsBackup.restore(Pair.create(fakeDetails, new Boolean[] {true}), customFile)
                .blockingSubscribe(p -> hits.get().add(p));

        Assert.assertEquals(1, hits.get().size());
        Assert.assertSame(fakePrefsProvider, hits.get().get(0).provider);
        Assert.assertNotNull(fakePrefsProvider.storedPrefsRoot);
    }

    @Test
    public void testGetAllPrefsProviders() {
        final List<GlobalPrefsBackup.ProviderDetails> allPrefsProviders =
                GlobalPrefsBackup.getAllPrefsProviders(getApplicationContext());
        Assert.assertNotNull(allPrefsProviders);
        Assert.assertEquals(4, allPrefsProviders.size());
    }

    @Test
    public void testBackupRestoreHappyPath() throws Exception {
        final Context context = ApplicationProvider.getApplicationContext();
        final FakePrefsProvider fakePrefsProvider = new FakePrefsProvider("id1");
        final PrefsRoot originalPrefsRoot = fakePrefsProvider.getPrefsRoot();
        List<GlobalPrefsBackup.ProviderDetails> fakeDetails =
                Collections.singletonList(
                        new GlobalPrefsBackup.ProviderDetails(
                                fakePrefsProvider, R.string.pop_text_type_title));

        final AtomicReference<List<GlobalPrefsBackup.ProviderDetails>> hits =
                new AtomicReference<>(new ArrayList<>());
        GlobalPrefsBackup.backup(
                        Pair.create(fakeDetails, new Boolean[] {true}),
                        GlobalPrefsBackup.getDefaultBackupFile(context))
                .blockingSubscribe(p -> hits.get().add(p));
        Assert.assertEquals(1, hits.get().size());
        Assert.assertSame(fakePrefsProvider, hits.get().get(0).provider);

        hits.get().clear();

        final File backupFile =
                AnyApplication.getBackupFile(
                        ApplicationProvider.getApplicationContext(),
                        GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME);
        Assert.assertTrue(backupFile.exists());
        Assert.assertTrue(backupFile.length() > 0);

        Assert.assertNull(fakePrefsProvider.storedPrefsRoot);
        GlobalPrefsBackup.restore(
                        Pair.create(fakeDetails, new Boolean[] {true}),
                        GlobalPrefsBackup.getDefaultBackupFile(context))
                .blockingSubscribe(p -> hits.get().add(p));

        Assert.assertEquals(1, hits.get().size());
        Assert.assertSame(fakePrefsProvider, hits.get().get(0).provider);
        Assert.assertNotNull(fakePrefsProvider.storedPrefsRoot);
        Assert.assertNotSame(originalPrefsRoot, fakePrefsProvider.storedPrefsRoot);
        assertRootsEqual(originalPrefsRoot, fakePrefsProvider.storedPrefsRoot);
    }

    @Test
    public void testOnlyBackupRestoreEnabledProviders() {
        final Context context = ApplicationProvider.getApplicationContext();
        List<GlobalPrefsBackup.ProviderDetails> fakesDetails = new ArrayList<>(5);
        final FakePrefsProvider[] fakePrefsProviders = new FakePrefsProvider[5];
        final PrefsRoot[] originalRoots = new PrefsRoot[fakePrefsProviders.length];
        for (int providerIndex = 0; providerIndex < fakePrefsProviders.length; providerIndex++) {
            fakePrefsProviders[providerIndex] = new FakePrefsProvider("id_" + providerIndex);
            originalRoots[providerIndex] = fakePrefsProviders[providerIndex].getPrefsRoot();
            fakesDetails.add(
                    new GlobalPrefsBackup.ProviderDetails(
                            fakePrefsProviders[providerIndex], R.string.pop_text_type_title));
        }

        final AtomicReference<List<GlobalPrefsBackup.ProviderDetails>> hits =
                new AtomicReference<>(new ArrayList<>());
        final Boolean[] providersToBackup = {true, true, true, false, true};
        GlobalPrefsBackup.backup(
                        Pair.create(fakesDetails, providersToBackup),
                        GlobalPrefsBackup.getDefaultBackupFile(context))
                .blockingSubscribe(p -> hits.get().add(p));
        Assert.assertEquals(4, hits.get().size());
        Assert.assertSame(fakesDetails.get(0).provider, hits.get().get(0).provider);
        Assert.assertSame(fakesDetails.get(1).provider, hits.get().get(1).provider);
        Assert.assertSame(fakesDetails.get(2).provider, hits.get().get(2).provider);
        Assert.assertSame(fakesDetails.get(4).provider, hits.get().get(3).provider);

        hits.get().clear();
        // restoring the first and last. Also asking for restore of the 4th, which is not in the
        // list
        final Boolean[] providersToRestore = {true, false, false, true, true};
        GlobalPrefsBackup.restore(
                        Pair.create(fakesDetails, providersToRestore),
                        GlobalPrefsBackup.getDefaultBackupFile(context))
                .blockingSubscribe(p -> hits.get().add(p));
        Assert.assertEquals(3, hits.get().size());
        Assert.assertSame(fakesDetails.get(0).provider, hits.get().get(0).provider);
        Assert.assertSame(fakesDetails.get(3).provider, hits.get().get(1).provider);
        Assert.assertSame(fakesDetails.get(4).provider, hits.get().get(2).provider);

        for (int providerIndex = 0; providerIndex < fakePrefsProviders.length; providerIndex++) {
            final FakePrefsProvider fakePrefsProvider = fakePrefsProviders[providerIndex];
            if (providersToRestore[providerIndex] && providersToBackup[providerIndex]) {
                Assert.assertNotNull(
                        "Provider at index " + providerIndex + " should have been restored!",
                        fakePrefsProvider.storedPrefsRoot);
                Assert.assertNotSame(
                        "Provider at index " + providerIndex,
                        originalRoots[providerIndex],
                        fakePrefsProvider.storedPrefsRoot);
                assertRootsEqual(originalRoots[providerIndex], fakePrefsProvider.storedPrefsRoot);
            } else {
                Assert.assertNull(
                        "Provider at index " + providerIndex, fakePrefsProvider.storedPrefsRoot);
            }
        }
    }

    static class FakePrefsProvider implements PrefsProvider {
        private final String mId;
        @Nullable public PrefsRoot storedPrefsRoot;

        FakePrefsProvider(String id) {

            mId = id;
        }

        @Override
        public PrefsRoot getPrefsRoot() {
            PrefsRoot root = new PrefsRoot(2);
            root.addValue("test", "value");
            root.addValue("ctorId", mId);
            root.createChild().addValue("child", "child-value");
            return root;
        }

        @Override
        public String providerId() {
            return mId;
        }

        @Override
        public void storePrefsRoot(PrefsRoot prefsRoot) {
            storedPrefsRoot = prefsRoot;
        }
    }
}
