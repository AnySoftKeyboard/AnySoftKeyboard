# Development

To develop this project, you'll need:
 * Java 10 - get [OpenJDK](https://jdk.java.net/archive/) or [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk10&jvmVariant=hotspot). If you are using [jEnv](https://www.jenv.be/) then we have a `.java-version` file already set up.
 * Make sure you have the environment variable `JAVA_OPTS='-XX:+IgnoreUnrecognizedVMOptions --add-modules java.se.ee'`. This is required to ensure the JVM is merciful with libraries that were not designed for Java9+ JVMs.
 * git
 * [Android Studio](https://developer.android.com/studio/install)
 * Android SDK API Level 28 installed.
    * Set up `local.properties` file to point to it (that is, ensure you have `sdk.dir=/path/to/android/sdk` in it).
 * Android NDK [r14b](https://developer.android.com/ndk/downloads/older_releases.html) installed.
    Set up `local.properties` file to point to it (that is, ensure you have `ndk.dir=/path/to/android/ndk` in it).

## Continuous Integration and Pull-Requests Verifications
Each PR is verified by multiple tasks:
 * Code Style is verified by _Google Java Format_. You can auto-format changes by running `./gradlew googleJavaFormat`.
 * Static-Analysis is done with checkstyle, lint and Error-Prone. Run `./scripts/ci/ci_check.sh` to execute all checks.
 * Unit-Tests are verified using `./gradlew testDebugUnitTest`. 

## Components
* Main app (AnySoftKeyboard) is located under `ime/`.
* All add-ons are under `addons/`.
    * Language-packs are under `addons/languages/`.
    * Themes are under `addons/themes/`.
    * Quick-Text (e.g., emojis) are under `addons/quicktext/`.
    * Contributing document for add-ons can be found [here](addons/CONTRIBUTING.md).

# License
The components in this repository are released under the [Apache2](https://www.apache.org/licenses/LICENSE-2.0) license.
By contributing to this repository you give all copyright and distribution rights or AnySoftKeyboard maintainer.