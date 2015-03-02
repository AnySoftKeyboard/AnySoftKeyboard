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

package com.anysoftkeyboard.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class CompatUtils {
    private static final String TAG = "ASK CMPT_UTILS";

    public static Method getMethod(Class<?> targetClass, String name,
                                   Class<?>... parameterTypes) {
        if (targetClass == null || TextUtils.isEmpty(name)) return null;
        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return null;
    }

    public static Object invoke(Object receiver, Object defaultValue, Method method, Object... args) {
        if (method == null) return defaultValue;
        try {
            return method.invoke(receiver, args);
        } catch (Exception e) {
            Log.e(TAG, "Exception in invoke: " + e.getClass().getSimpleName());
        }
        return defaultValue;
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static Locale getLocaleForLanguageTag(@Nullable String locale) {
		Locale parsedLocale = Locale.getDefault();
		if (!TextUtils.isEmpty(locale)) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					parsedLocale = Locale.forLanguageTag(locale);
				} else {
					parsedLocale = new Locale(locale);
				}
			} catch (Exception e) {
				Log.d(TAG, "Failed to parse locale '%s'. Defaulting to %s", parsedLocale);
			}
		}
		return parsedLocale;
	}
}
