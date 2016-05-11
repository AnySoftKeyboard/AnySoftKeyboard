package com.menny.android.anysoftkeyboard;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.Method;

public class AskGradleTestRunner extends RobolectricGradleTestRunner {
    private static final int MAX_SDK_LEVEL = 21;

    public AskGradleTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected int pickSdkVersion(Config config, AndroidManifest manifest) {
        return ensureSdkLevel(super.pickSdkVersion(config, manifest));
    }

    @Override
    public Config getConfig(Method method) {
        Config config = super.getConfig(method);
        /* Fixing up the Config:
		* constants must point to a real BuildConfig class
		 */
        config = new Config.Implementation(
                config.sdk(),
                config.manifest(),
                config.qualifiers(),
                config.packageName(),
                config.resourceDir(),
                config.assetDir(),
                addInputMethodManagerShadow(config.shadows()),
                config.application(),
                config.libraries(),
                ensureBuildConfig(config.constants()));

        return config;
    }

    private static Class<?>[] addInputMethodManagerShadow(Class<?>[] shadows) {
        Class<?>[] newShadows = new Class<?>[shadows.length + 2];

        System.arraycopy(shadows, 0, newShadows, 0, shadows.length);

        newShadows[newShadows.length-2] = InputMethodManagerShadow.class;
        newShadows[newShadows.length-1] = AsyncTaskShadow.class;
        return newShadows;
    }

    private Class<?> ensureBuildConfig(Class<?> constants) {
        if (constants == Void.class) return BuildConfig.class;
        return constants;
    }

    private int ensureSdkLevel(int sdkLevel) {
        if (sdkLevel > MAX_SDK_LEVEL) return MAX_SDK_LEVEL;
        if (sdkLevel <= 0) return MAX_SDK_LEVEL;
        return sdkLevel;
    }
}
