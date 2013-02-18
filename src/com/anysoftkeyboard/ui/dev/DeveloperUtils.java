package com.anysoftkeyboard.ui.dev;

import java.io.File;
import java.io.IOException;

import android.os.Debug;
import android.os.Environment;

public class DeveloperUtils {

	public static File createMemoryDump() throws IOException,
			UnsupportedOperationException {
		File extFolder = Environment.getExternalStorageDirectory();
		File target = new File(extFolder, "ask_mem_dump.hprof");
		target.delete();
		Debug.dumpHprofData(target.getAbsolutePath());
		return target;
	}
}