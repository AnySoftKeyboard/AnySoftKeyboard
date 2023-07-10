package com.anysoftkeyboard.addons.ui;

/*
 * Copyright (c) 2023 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.anysoftkeyboard.addons.R;
import com.anysoftkeyboard.base.utils.Logger;
import net.evendanan.pixel.GeneralDialogController;

public class AddOnStoreSearchController {
  private static final String TAG = "AddOnStoreSearchController";

  private static final int LEAVE = 12121;
  private static final int NO_MARKET = 34534;

  private final GeneralDialogController mDialogController;
  private final Context mContext;
  private final String mMarketKeyword;

  public AddOnStoreSearchController(@NonNull Context context, @NonNull String keyword) {
    mContext = context;
    mMarketKeyword = keyword;
    mDialogController =
        new GeneralDialogController(context, R.style.Theme_AskAlertDialog, this::setupDialog);
  }

  private void setupDialog(
      Context context, AlertDialog.Builder builder, int optionId, @Nullable Object data) {
    switch (optionId) {
      case LEAVE:
        builder
            .setIcon(android.R.drawable.stat_sys_warning)
            .setTitle(R.string.leaving_ask_to_market_title)
            .setMessage(R.string.leaving_ask_to_market_message)
            .setPositiveButton(R.string.cta_continue_to_market, this::continueToMarket)
            .setNegativeButton(android.R.string.cancel, null);
        break;
      case NO_MARKET:
        builder
            .setIcon(android.R.drawable.stat_sys_warning)
            .setTitle(R.string.no_market_store_available_title)
            .setMessage(R.string.no_market_store_available)
            .setNegativeButton(android.R.string.cancel, null);
        break;
      default:
        throw new RuntimeException("Unknown optionID");
    }
  }

  public void searchForAddOns() {
    mDialogController.showDialog(LEAVE);
  }

  public void dismiss() {
    mDialogController.dismiss();
  }

  private void continueToMarket(DialogInterface di, int buttonId) {
    if (!startMarketActivity(mContext, mMarketKeyword)) {
      mDialogController.showDialog(NO_MARKET);
    }
  }

  protected static boolean startMarketActivity(
      @NonNull Context context, @NonNull String marketKeyword) {
    try {
      Intent search = new Intent(Intent.ACTION_VIEW);
      Uri uri =
          new Uri.Builder()
              .scheme("market")
              .authority("search")
              .appendQueryParameter("q", "AnySoftKeyboard " + marketKeyword)
              .build();
      search.setData(uri);
      search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(search);
    } catch (Exception ex) {
      Logger.e(TAG, "Could not launch Store search!", ex);
      return false;
    }
    return true;
  }
}
