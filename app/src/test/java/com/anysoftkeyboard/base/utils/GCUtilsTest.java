package com.anysoftkeyboard.base.utils;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(AnySoftKeyboardTestRunner.class)
public class GCUtilsTest {

    private TestableGCUtils mUnderTest;
    private GCUtils.MemRelatedOperation mOperation;

    @Before
    public void setUp() {
        mUnderTest = new TestableGCUtils();
        mOperation = Mockito.mock(GCUtils.MemRelatedOperation.class);
    }

    @Test
    public void testNoRetry() {
        mUnderTest.performOperationWithMemRetry("test", mOperation);
        Mockito.verify(mOperation).operation();
        Assert.assertEquals(0, mUnderTest.mGarbageCollectionDone);
    }

    @Test(expected = OutOfMemoryError.class)
    public void testMaxRetryAndThrowException() {
        setupOutOfMemoryError(GCUtils.GC_TRY_LOOP_MAX + 1);
        mUnderTest.performOperationWithMemRetry("test", mOperation);
    }

    @Test
    public void testSomeRetry() {
        setupOutOfMemoryError(GCUtils.GC_TRY_LOOP_MAX - 1);

        mUnderTest.performOperationWithMemRetry("test", mOperation);
        Mockito.verify(mOperation, Mockito.times(GCUtils.GC_TRY_LOOP_MAX)).operation();
        Assert.assertEquals(GCUtils.GC_TRY_LOOP_MAX - 1, mUnderTest.mGarbageCollectionDone);
    }

    @Test
    public void testOneRetry() {
        setupOutOfMemoryError(1);

        mUnderTest.performOperationWithMemRetry("test", mOperation);
        Mockito.verify(mOperation, Mockito.times(2)).operation();
        Assert.assertEquals(1, mUnderTest.mGarbageCollectionDone);
    }

    @Test
    public void testDoGarbageCollectionDoesNotFail() {
        new GCUtils().doGarbageCollection("tag");
    }

    private void setupOutOfMemoryError(final int failuresCount) {
        Mockito.doAnswer(new FailureAnswer(failuresCount)).when(mOperation).operation();
    }

    private static class TestableGCUtils extends GCUtils {
        private int mGarbageCollectionDone;

        @Override
        void doGarbageCollection(String tag) {
            mGarbageCollectionDone++;
        }
    }

    private static class FailureAnswer implements Answer {
        private int mFailuresLeft;

        FailureAnswer(int failuresCount) {
            mFailuresLeft = failuresCount;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            if (mFailuresLeft > 0) {
                mFailuresLeft--;
                throw new OutOfMemoryError();
            }
            return null;
        }
    }
}