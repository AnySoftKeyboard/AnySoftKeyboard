/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.chewbacca;

import static com.anysoftkeyboard.base.utils.Logger.NEW_LINE;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.NonNull;

public abstract class ChewbaccaUtils {
  @NonNull
  public static String getSysInfo(@NonNull Context context) {
    StringBuilder sb = new StringBuilder();
    sb.append("BRAND:").append(Build.BRAND).append(NEW_LINE);
    sb.append("DEVICE:").append(Build.DEVICE).append(NEW_LINE);
    sb.append("Build ID:").append(Build.DISPLAY).append(NEW_LINE);
    sb.append("changelist number:").append(Build.ID).append(NEW_LINE);
    sb.append("MODEL:").append(Build.MODEL).append(NEW_LINE);
    sb.append("PRODUCT:").append(Build.PRODUCT).append(NEW_LINE);
    sb.append("TAGS:").append(Build.TAGS).append(NEW_LINE);
    sb.append("VERSION.INCREMENTAL:").append(Build.VERSION.INCREMENTAL).append(NEW_LINE);
    sb.append("VERSION.RELEASE:").append(Build.VERSION.RELEASE).append(NEW_LINE);
    sb.append("VERSION.SDK_INT:").append(Build.VERSION.SDK_INT).append(NEW_LINE);
    Configuration configuration = context.getResources().getConfiguration();
    sb.append("Locale:").append(configuration.locale).append(NEW_LINE);
    sb.append("configuration:").append(configuration).append(NEW_LINE);
    sb.append("That's all I know.");
    return sb.toString();
  }
}
