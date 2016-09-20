package com.anysoftkeyboard.ui.tutorials;

import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class VersionChangeLogsTest {
    @Test
    public void createChangeLog() throws Exception {
        List<VersionChangeLogs.VersionChangeLog> logs = VersionChangeLogs.createChangeLog();
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() > 0);

        Set<String> seenVersions = new HashSet<>();
        Set<String> seenUrls = new HashSet<>();
        for (VersionChangeLogs.VersionChangeLog aLog : logs) {
            Assert.assertTrue(aLog.changes.length > 0);
            Assert.assertFalse(TextUtils.isEmpty(aLog.versionName));
            Assert.assertFalse(seenVersions.contains(aLog.versionName));
            seenVersions.add(aLog.versionName);

            Assert.assertFalse(TextUtils.isEmpty(aLog.changesWebUrl.toString()));
            Assert.assertFalse(seenUrls.contains(aLog.changesWebUrl.toString()));
            seenUrls.add(aLog.changesWebUrl.toString());
        }
    }

}