package com.anysoftkeyboard.ui.dev;

import java.io.File;
import java.io.IOException;

import com.anysoftkeyboard.utils.Workarounds;
import com.menny.android.anysoftkeyboard.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;

public class DeveloperUtils {

	private static final String KEY_SDCARD_TRACING_ENABLED = "KEY_SDCARD_TRACING_ENABLED";
	private static final String ASK_TRACE_FILENAME = "AnySoftKeyboard_tracing.trace";
	private static final String ASK_MEM_DUMP_FILENAME = "ask_mem_dump.hprof";
	
	public static File createMemoryDump() throws IOException,
			UnsupportedOperationException {
		File extFolder = Environment.getExternalStorageDirectory();
		File target = new File(extFolder, ASK_MEM_DUMP_FILENAME);
		target.delete();
		Debug.dumpHprofData(target.getAbsolutePath());
		return target;
	}

	public static boolean hasTracingRequested(Context applicationContext) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		return prefs.getBoolean(KEY_SDCARD_TRACING_ENABLED, false);
	}
	
	public static void setTracingRequested(Context applicationContext, boolean enabled) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		Editor e = prefs.edit();
		e.putBoolean(KEY_SDCARD_TRACING_ENABLED, enabled);
		e.commit();
	}

	private static boolean msTracingStarted = false;
	
	public static void startTracing() {
		Debug.startMethodTracing(getTraceFile().getAbsolutePath());
		msTracingStarted = true;
	}
	
	public static boolean hasTracingStarted() {
		return msTracingStarted;
	}

	public static void stopTracing() {
		Debug.stopMethodTracing();
		msTracingStarted = false;
	}

	public static File getTraceFile() {
		File extFolder = Environment.getExternalStorageDirectory();
		File target = new File(extFolder, ASK_TRACE_FILENAME);
		return target;
	}
	
	public static String getSysInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("BRAND:").append(android.os.Build.BRAND).append("\n");
		sb.append("DEVICE:").append(android.os.Build.DEVICE).append("\n");
		sb.append("Build ID:").append(android.os.Build.DISPLAY).append("\n");
		sb.append("changelist number:").append(android.os.Build.ID)
				.append("\n");
		sb.append("MODEL:").append(android.os.Build.MODEL).append("\n");
		sb.append("PRODUCT:").append(android.os.Build.PRODUCT).append("\n");
		sb.append("TAGS:").append(android.os.Build.TAGS).append("\n");
		sb.append("VERSION.INCREMENTAL:")
				.append(android.os.Build.VERSION.INCREMENTAL).append("\n");
		sb.append("VERSION.RELEASE:").append(android.os.Build.VERSION.RELEASE)
				.append("\n");
		sb.append("VERSION.SDK_INT:").append(Workarounds.getApiLevel())
				.append("\n");
		sb.append("That's all I know.\n");
		return sb.toString();
	}
	public static String getAppDetails(Context appContext) {
		String appName = appContext.getText(R.string.ime_name).toString();
		try {
			PackageInfo info = appContext.getPackageManager().getPackageInfo(
					appContext.getPackageName(), 0);
			appName = appName + " v" + info.versionName + " release "
					+ info.versionCode;
		} catch (NameNotFoundException e) {
			appName = "NA";
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return appName;
	}
}