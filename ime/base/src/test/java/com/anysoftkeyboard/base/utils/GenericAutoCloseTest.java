package com.anysoftkeyboard.base.utils;

import android.view.inputmethod.InputConnection;
import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class GenericAutoCloseTest {

  @Test
  public void testGeneric() {
    final var isClosed = new AtomicBoolean(false);
    final var closer =
        GenericAutoClose.close(
            () -> {
              isClosed.set(true);
            });
    try (closer) {
      Assert.assertFalse(isClosed.get());
    }
    Assert.assertTrue(isClosed.get());
  }

  @Test
  public void testInputConnectionClosable() {
    final var icMock = Mockito.mock(InputConnection.class);
    Mockito.verify(icMock, Mockito.never()).beginBatchEdit();
    Mockito.verify(icMock, Mockito.never()).endBatchEdit();
    try (var closer = GenericAutoClose.batchEdit(icMock)) {
      Assert.assertNotNull(closer);
      Mockito.verify(icMock).beginBatchEdit();
      Mockito.verify(icMock, Mockito.never()).endBatchEdit();
    }
    Mockito.verify(icMock).beginBatchEdit();
    Mockito.verify(icMock).endBatchEdit();
  }
}
