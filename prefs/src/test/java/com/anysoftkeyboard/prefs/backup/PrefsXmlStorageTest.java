package com.anysoftkeyboard.prefs.backup;

import android.support.v4.util.Pair;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import com.anysoftkeyboard.test.TestUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class PrefsXmlStorageTest {

    private File mFile;
    private PrefsXmlStorage mUnderTest;

    @Before
    public void setup() throws Exception {
        mFile = File.createTempFile("PrefsXmlStorageTest", ".xml");
        mUnderTest = new PrefsXmlStorage(mFile);
    }

    @After
    public void tearDown() throws Exception {
        if (mFile != null) {
            mFile.deleteOnExit();
        }
    }

    @Test
    public void testHappyPath() throws Exception {
        PrefsRoot root = new PrefsRoot(3);
        final PrefItem prefItem = root.createChild();
        prefItem.addValue("key", "value");

        final PrefItem childItem = prefItem.createChild();
        childItem.addValue("keyChild1", "value 1");
        childItem.addValue("keyChild2", "value 2");

        final PrefItem childItem2 = prefItem.createChild();
        childItem2.addValue("keyChild3", "value 3");
        childItem2.addValue("keyChild4", "value 4");

        Assert.assertEquals(3, root.getVersion());

        mUnderTest.store(root);

        PrefsRoot loadedRoot = mUnderTest.load();
        Assert.assertNotNull(loadedRoot);

        Assert.assertEquals(3, loadedRoot.getVersion());
        Assert.assertEquals(1, TestUtils.convertToList(loadedRoot.getChildren()).size());
        final PrefItem rootItem = TestUtils.convertToList(loadedRoot.getChildren()).get(0);
        Assert.assertEquals(1, TestUtils.convertToList(rootItem.getValues()).size());
        Assert.assertEquals(Pair.create("key", "value"), TestUtils.convertToList(rootItem.getValues()).get(0));
        Assert.assertEquals(2, TestUtils.convertToList(rootItem.getChildren()).size());
        final PrefItem child1 = TestUtils.convertToList(rootItem.getChildren()).get(0);
        Assert.assertEquals(2, TestUtils.convertToList(child1.getValues()).size());
        Assert.assertEquals(Pair.create("keyChild1", "value 1"), TestUtils.convertToList(child1.getValues()).get(0));
        Assert.assertEquals(Pair.create("keyChild2", "value 2"), TestUtils.convertToList(child1.getValues()).get(1));
        Assert.assertEquals(0, TestUtils.convertToList(child1.getChildren()).size());
        final PrefItem child2 = TestUtils.convertToList(rootItem.getChildren()).get(1);
        Assert.assertEquals(2, TestUtils.convertToList(child2.getValues()).size());
        Assert.assertEquals(Pair.create("keyChild3", "value 3"), TestUtils.convertToList(child2.getValues()).get(0));
        Assert.assertEquals(Pair.create("keyChild4", "value 4"), TestUtils.convertToList(child2.getValues()).get(1));
        Assert.assertEquals(0, TestUtils.convertToList(child2.getChildren()).size());
    }

    @Test
    public void testStoreOverwrites() throws Exception {
        PrefsRoot rootTemp = new PrefsRoot(3);
        final PrefItem tempPrefItem = rootTemp.createChild();
        tempPrefItem.addValue("g", "a");
        tempPrefItem.createChild().addValue("inside", "value");

        mUnderTest.store(rootTemp);

        PrefsRoot root = new PrefsRoot(2);
        final PrefItem prefItem = root.createChild();
        prefItem.addValue("a", "b");

        mUnderTest.store(root);

        PrefsRoot loadedRoot = mUnderTest.load();
        Assert.assertEquals(2, loadedRoot.getVersion());
        Assert.assertEquals(1, TestUtils.convertToList(loadedRoot.getChildren()).size());
        Assert.assertEquals(1, TestUtils.convertToList(TestUtils.convertToList(loadedRoot.getChildren()).get(0).getValues()).size());
        Assert.assertEquals(0, TestUtils.convertToList(TestUtils.convertToList(loadedRoot.getChildren()).get(0).getChildren()).size());
        Assert.assertEquals(Pair.create("a", "b"), TestUtils.convertToList(TestUtils.convertToList(loadedRoot.getChildren()).get(0).getValues()).get(0));
    }
}