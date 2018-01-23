package com.anysoftkeyboard.ui.tutorials;

import android.text.TextUtils;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class VersionChangeLogsTest {
    @Test
    public void createChangeLog() throws Exception {
        List<VersionChangeLogs.VersionChangeLog> logs = VersionChangeLogs.createChangeLog();
        Assert.assertNotNull(logs);
        Assert.assertTrue(logs.size() > 0);

        Set<String> seenVersions = new HashSet<>();
        Set<String> seenUrls = new HashSet<>();
        for (VersionChangeLogs.VersionChangeLog log : logs) {
            Assert.assertTrue(log.changes.length > 0);
            Assert.assertFalse(TextUtils.isEmpty(log.versionName));
            Assert.assertFalse(seenVersions.contains(log.versionName));
            seenVersions.add(log.versionName);

            Assert.assertFalse(TextUtils.isEmpty(log.changesWebUrl.toString()));
            Assert.assertFalse(seenUrls.contains(log.changesWebUrl.toString()));
            seenUrls.add(log.changesWebUrl.toString());
        }
    }

}