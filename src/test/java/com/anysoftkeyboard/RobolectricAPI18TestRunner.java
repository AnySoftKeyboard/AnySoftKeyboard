package com.anysoftkeyboard;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class RobolectricAPI18TestRunner extends RobolectricTestRunner {

	/**
	 * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
	 * and res directory by default. Use the {@link org.robolectric.annotation.Config} annotation to configure.
	 *
	 * @param testClass the test class to be run
	 * @throws org.junit.runners.model.InitializationError if junit says so
	 */
	public RobolectricAPI18TestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	protected AndroidManifest getAppManifest(Config config) {
		String manifestProperty = System.getProperty("android.manifest");
		String resProperty      = System.getProperty("android.resources");
		String assetsProperty   = System.getProperty("android.assets");

		FsFile manifestFile = Fs.fileFromPath(manifestProperty);
		FsFile resDir       = Fs.fileFromPath(resProperty);
		FsFile assetsDir    = Fs.fileFromPath(assetsProperty);

		return new AndroidManifest(manifestFile, resDir, assetsDir) {
			@Override
			public int getTargetSdkVersion() {
				// Robolectric currently doesn't support API level > 18, so in this case, pretend
				// that we're on API level 18, cross our fingers, and hope that we don't have a
				// test that depend on API level 19...
				return 18;
			}
		};
	}
}
