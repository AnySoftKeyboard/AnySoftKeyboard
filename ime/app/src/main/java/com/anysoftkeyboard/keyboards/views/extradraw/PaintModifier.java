package com.anysoftkeyboard.keyboards.views.extradraw;

import android.graphics.Paint;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;

public interface PaintModifier<T> {
  Paint modify(Paint original, AnyKeyboardViewWithExtraDraw ime, T extraData);
}
