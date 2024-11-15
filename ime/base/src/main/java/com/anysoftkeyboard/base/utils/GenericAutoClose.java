package com.anysoftkeyboard.base.utils;

import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;

public class GenericAutoClose implements AutoCloseable {

  private final Runnable mCloseFunction;

  public static GenericAutoClose close(@NonNull Runnable closeFunction) {
    return new GenericAutoClose(closeFunction);
  }

  public static GenericAutoClose batchEdit(@NonNull InputConnection ic) {
    ic.beginBatchEdit();
    return new ICAutoClose(ic);
  }

  private GenericAutoClose(@NonNull Runnable closeFunction) {
    mCloseFunction = closeFunction;
  }

  @Override
  public void close() {
    mCloseFunction.run();
  }

  private static class ICAutoClose extends GenericAutoClose {
    private ICAutoClose(@NonNull InputConnection ic) {
      super(ic::endBatchEdit);
    }
  }
}
