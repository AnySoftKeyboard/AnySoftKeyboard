package com.anysoftkeyboard;

import static com.google.common.base.Charsets.UTF_8;

import android.os.Looper;

import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.google.common.io.Files;

import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;

import org.bouncycastle.util.encoders.Hex;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.android.util.concurrent.RoboExecutorService;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.annotation.Nonnull;

/**
 * Just a way to add general things on-top RobolectricTestRunner.
 */

@SuppressWarnings("CatchAndPrintStackTrace")
public class AnySoftKeyboardRobolectricTestRunner extends RobolectricTestRunner {
    public AnySoftKeyboardRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(this, new TestClassHashingStrategy(), false/*so running from AS will work*/);

    }

    @Nonnull
    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
        return AnySoftKeyboardRobolectricTestLifeCycle.class;
    }

    public static class AnySoftKeyboardRobolectricTestLifeCycle extends DefaultTestLifecycle {
        @Override
        public void beforeTest(Method method) {
            TestRxSchedulers.setSchedulers(Looper.getMainLooper(), new RoboExecutorService());
            super.beforeTest(method);
        }
    }

    /*
        Trying to overcome https://github.com/robolectric/robolectric/issues/3659
     */

    @Override
    protected DependencyResolver getJarResolver() {
        return new FileLockingDependencyResolver(super.getJarResolver());
    }

    private static class FileLockingDependencyResolver implements DependencyResolver {

        private static String calcSHA1(File file) {
            try {
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                try (InputStream input = new FileInputStream(file)) {

                    final byte[] buffer = new byte[8192];
                    int len = input.read(buffer);

                    while (len != -1) {
                        sha1.update(buffer, 0, len);
                        len = input.read(buffer);
                    }

                    return Hex.toHexString(sha1.digest());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            return "";
        }

        private static final Unlockable NULL_LOCK = new NullUnlockable();

        private final DependencyResolver mJarResolver;

        public FileLockingDependencyResolver(DependencyResolver jarResolver) {
            mJarResolver = jarResolver;
        }

        @Override
        public URL getLocalArtifactUrl(DependencyJar dependency) {
            final Unlockable lock = obtainLock();
            try {
                return getLocalArtifactUrlWithVerificationAndReTries(dependency, 2);
            } finally {
                lock.unlock();
            }
        }

        private URL getLocalArtifactUrlWithVerificationAndReTries(DependencyJar dependency, final int retriesLeft) {
            final URL localArtifactUrl = mJarResolver.getLocalArtifactUrl(dependency);
            final File artifactFile = new File(localArtifactUrl.getFile());
            if (artifactFile.getName().endsWith(".jar")) {
                final String jarSha1 = calcSHA1(artifactFile);
                System.err.println(String.format(Locale.US, "Robolectric artifact is %s, size %d, sha1 %s", artifactFile.getAbsolutePath(), artifactFile.length(), jarSha1));
                final File sha1File = new File(artifactFile.getAbsolutePath() + ".sha1");
                final String sha1FromFile = getSha1ForFileFromLocalRepo(sha1File);
                if (sha1FromFile.trim().toLowerCase(Locale.US).equals(jarSha1.trim().toLowerCase(Locale.US))) {
                    System.err.println(String.format(Locale.US, "Robolectric artifact SHA1 suppose to be '%s' and is '%s'", sha1FromFile, jarSha1));
                    return localArtifactUrl;
                } else {
                    System.err.println(
                            String.format(Locale.US, "Robolectric artifact '%s' SHA1 suppose to be '%s' but is '%s'. Trying to re-download.", artifactFile.getAbsolutePath(), sha1FromFile, jarSha1));
                    if (artifactFile.exists() && !artifactFile.delete()) {
                        throw new IllegalStateException(String.format(Locale.US, "Failed to delete invalid Robolectric artifact '%s'!", artifactFile.getAbsolutePath()));
                    }

                    if (retriesLeft == 0) {
                        throw new IllegalStateException(String.format(Locale.US, "Failed to get a valid Robolectric artifact '%s' with SHA1 '%s'.", artifactFile.getAbsolutePath(), sha1FromFile));
                    } else {
                        return getLocalArtifactUrlWithVerificationAndReTries(dependency, retriesLeft - 1);
                    }
                }
            } else {
                return localArtifactUrl;
            }
        }

        private static String getSha1ForFileFromLocalRepo(File sha1File) {
            try {
                return Files.readFirstLine(sha1File, UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(String.format(Locale.US, "Failed to read SHA1 value from '%s'. Message: %s", sha1File.getAbsolutePath(), e.getMessage()), e);
            }
        }

        private static Unlockable obtainLock() {
            //in case the system-property was not set, we'll use the OS's temp folder.
            final File file = new File(System.getProperty("robolectric.dependency.lock_file"), System.getProperty("java.io.tmpdir") + "/robolectric.deps.lock");

            System.err.println(String.format(Locale.US, "Locking Robolectric cache using '%s'...", file));
            try {
                final RandomAccessFile lockFile = new RandomAccessFile(file, "rw");
                final FileChannel fileChannel = lockFile.getChannel();
                final FileLock lock = fileChannel.lock();
                System.err.println(String.format(Locale.US, "Obtained Robolectric cache lock at '%s'.", file));
                return new RealUnlockable(lock, file, lockFile);
            } catch (IOException e) {
                System.err.println(String.format(Locale.US, "Failed to obtain a lock at '%s' with error '%s'", file, e));
                return NULL_LOCK;
            }
        }

        private static class RealUnlockable implements Unlockable {
            private final FileLock mLock;
            private final File mFile;
            private final RandomAccessFile mLockFile;

            public RealUnlockable(FileLock lock, File file, RandomAccessFile lockFile) {
                mLock = lock;
                mFile = file;
                mLockFile = lockFile;
            }

            @Override
            public void unlock() {
                try {
                    mLock.release();
                } catch (IOException e) {
                    System.err.println(String.format(Locale.US, "Failed to unlock '%s'", mFile));
                    e.printStackTrace();
                } finally {
                    try {
                        mLockFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private static class NullUnlockable implements Unlockable {
            @Override
            public void unlock() {
            }
        }
    }

    private interface Unlockable {
        void unlock();
    }
}
