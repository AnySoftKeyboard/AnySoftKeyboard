# Development

To develop this project, you'll need:

- Java - get [OpenJDK](https://jdk.java.net/) or [Eclipse Temurin](https://adoptium.net/temurin/releases/). If you are using [jEnv](https://www.jenv.be/) then we have a `.java-version` file already set up.
  - Do not hard-code the version here: the source-of-truth is `.java-version` and the CI setup (see `actions/setup-java` `java-version` in `.github/workflows/checks.yml`), plus `compileOptions` in `gradle/android_general.gradle` and `jvmTarget` in `buildSrc/build.gradle`.
- git
- A recent [Android Studio](https://developer.android.com/studio/install) version that supports the Android Gradle Plugin version declared in `gradle/libs.versions.toml` (`agp`).
- You will need to point your Android-Studio to use the installed Java. You can configure the path inside Android Studio menu: File > Project Structure > JDK Location.
- Android SDK. Do not rely on a version number written here: the source-of-truth is `gradle/libs.versions.toml` (`sdkMinimum`, `sdkTarget`, `sdkCompile`, `androidBuildTools`), wired via `gradle/root_all_projects_ext.gradle` and `gradle/android_general.gradle` (`compileSdkVersion`, `minSdkVersion`, `targetSdkVersion`).
- Android NDK. Do not rely on a version number written here: the source-of-truth is `sideBySideNdkVersion` in `gradle/root_all_projects_ext.gradle` (installed automatically as a side-by-side NDK package, no `ndk.dir` in `local.properties` needed).

## Continuous Integration and Pull-Requests Verifications

Each PR is verified by multiple tasks:

- Static-Analysis is done with checkstyle, lint and Error-Prone. Run `./scripts/ci/ci_check.sh` to execute all checks.
- Unit-Tests are verified using `./gradlew testDebugUnitTest`.

## Components

- Main app (AnySoftKeyboard) is located under `ime/`.
  - to build the APK use following command: `gradlew :ime:app:assembleDebug`
- All add-ons are under `addons/`.
  - Language-packs are under `addons/languages/`.
  - Themes are under `addons/themes/`.
  - Quick-Text (e.g., emojis) are under `addons/quicktext/`.
  - Contributing document for add-ons can be found [here](addons/CONTRIBUTING.md).

# License

The components in this repository are released under the [Apache2](https://www.apache.org/licenses/LICENSE-2.0) license.
By contributing to this repository you give all copyright and distribution rights or AnySoftKeyboard maintainer.<br/>
Of course, since this is Apache2, you may fork and do whatever you want with the code, you do not have to share back only give attribute.<br/>
Read more about this license [here](<https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)>).

# Contributors

If you decided to help out and contribute your time, we are greatly appreciate this.
Our contributors list can be found [here](CONTRIBUTORS.md).
Contributors should adhere to the [Code of Conduct](CODE_OF_CONDUCT.md) document.
