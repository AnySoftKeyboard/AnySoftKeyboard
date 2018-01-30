package com.anysoftkeyboard.prefs;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.base.Charsets;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.test.TestUtils;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GlobalPrefsBackupTest {

    static class FakePrefsProvider implements PrefsProvider {
        private final String mId;
        @Nullable
        public PrefsRoot storedPrefsRoot;

        FakePrefsProvider(String id) {

            mId = id;
        }

        @Override
        public PrefsRoot getPrefsRoot() {
            PrefsRoot root = new PrefsRoot(2);
            root.addValue("test", "value");
            root.addValue("ctorId", mId);
            root.createChild()
                    .addValue("child", "child-value");
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

    @Test
    public void testGetAllPrefsProviders() {
        final List<GlobalPrefsBackup.ProviderDetails> allPrefsProviders = GlobalPrefsBackup.getAllPrefsProviders(RuntimeEnvironment.application);
        Assert.assertNotNull(allPrefsProviders);
        Assert.assertEquals(1, allPrefsProviders.size());
    }

    @Test
    public void testBackupRestoreHappyPath() throws Exception {
        final FakePrefsProvider fakePrefsProvider = new FakePrefsProvider("id1");
        final PrefsRoot originalPrefsRoot = fakePrefsProvider.getPrefsRoot();
        List<GlobalPrefsBackup.ProviderDetails> fakeDetails = Collections.singletonList(
                new GlobalPrefsBackup.ProviderDetails(fakePrefsProvider, R.string.pop_text_type_title));

        GlobalPrefsBackup.backup(Observable.just(Pair.create(fakeDetails, new Boolean[]{true})))
                .blockingSubscribe(b -> { });

        System.out.println("AnyApplication.getBackupFile(GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME) is " + AnyApplication.getBackupFile(GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME).getAbsolutePath());
        System.out.println(Arrays.toString(Files.readAllLines(AnyApplication.getBackupFile(GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME).toPath(), Charsets.UTF8).toArray()));
        Assert.assertTrue(AnyApplication.getBackupFile(GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME).exists());
        Assert.assertTrue(AnyApplication.getBackupFile(GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME).length() > 0);

        Assert.assertNull(fakePrefsProvider.storedPrefsRoot);
        GlobalPrefsBackup.restore(Observable.just(Pair.create(fakeDetails, new Boolean[]{true})))
                .blockingSubscribe(b -> { });

        Assert.assertNotNull(fakePrefsProvider.storedPrefsRoot);
        Assert.assertNotSame(originalPrefsRoot, fakePrefsProvider.storedPrefsRoot);
        assertRootsEqual(originalPrefsRoot, fakePrefsProvider.storedPrefsRoot);
    }

    @Test
    public void testOnlyBackupRestoreEnabledProviders() {
        List<GlobalPrefsBackup.ProviderDetails> fakesDetails = new ArrayList<>(5);
        final FakePrefsProvider[] fakePrefsProviders = new FakePrefsProvider[5];
        final PrefsRoot[] originalRoots = new PrefsRoot[fakePrefsProviders.length];
        for (int providerIndex = 0; providerIndex < fakePrefsProviders.length; providerIndex++) {
            fakePrefsProviders[providerIndex] = new FakePrefsProvider("id_" + providerIndex);
            originalRoots[providerIndex] = fakePrefsProviders[providerIndex].getPrefsRoot();
            fakesDetails.add(new GlobalPrefsBackup.ProviderDetails(fakePrefsProviders[providerIndex], R.string.pop_text_type_title));
        }

        final Boolean[] providersToBackup = {true, true, true, false, true};
        GlobalPrefsBackup.backup(Observable.just(Pair.create(fakesDetails, providersToBackup)))
                .blockingSubscribe(b -> { });

        //restoring the first and last. Also asking for restore of the 4th, which is not in the list
        final Boolean[] providersToRestore = {true, false, false, true, true};
        GlobalPrefsBackup.restore(Observable.just(Pair.create(fakesDetails, providersToRestore)))
                .blockingSubscribe(b -> { });

        for (int providerIndex = 0; providerIndex < fakePrefsProviders.length; providerIndex++) {
            final FakePrefsProvider fakePrefsProvider = fakePrefsProviders[providerIndex];
            if (providersToRestore[providerIndex] && providersToBackup[providerIndex]) {
                Assert.assertNotNull("Provider at index " + providerIndex + " should have been restored!", fakePrefsProvider.storedPrefsRoot);
                Assert.assertNotSame("Provider at index " + providerIndex, originalRoots[providerIndex], fakePrefsProvider.storedPrefsRoot);
                assertRootsEqual(originalRoots[providerIndex], fakePrefsProvider.storedPrefsRoot);
            } else {
                Assert.assertNull("Provider at index " + providerIndex, fakePrefsProvider.storedPrefsRoot);
            }
        }
    }

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
}