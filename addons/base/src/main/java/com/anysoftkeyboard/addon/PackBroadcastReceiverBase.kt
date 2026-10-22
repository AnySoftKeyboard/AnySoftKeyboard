package com.anysoftkeyboard.addon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

open class PackBroadcastReceiverBase : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {}
}
