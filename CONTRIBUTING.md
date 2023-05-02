# Development

To develop this project, you'll need:
 * Java 11 - get [OpenJDK](https://jdk.java.net/archive/) or [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot). If you are using [jEnv](https://www.jenv.be/) then we have a `.java-version` file already set up.
 * git
 * [Android Studio](https://developer.android.com/studio/install) version 4.0.
 * You will need to point your Android-Studio to use the installed Java11. You can configure the path inside Android Studio menu: File > Project Structure > JDK Location.
 * Android SDK API Level 28 installed.
    * Set up `local.properties` file to point to it (that is, ensure you have `sdk.dir=/path/to/android/sdk` in it).
 * Android NDK [r14b](https://developer.android.com/ndk/downloads/older_releases.html) installed.
    Set up `local.properties` file to point to it (that is, ensure you have `ndk.dir=/path/to/android/ndk` in it).

## Continuous Integration and Pull-Requests Verifications
Each PR is verified by multiple tasks:
 * Static-Analysis is done with checkstyle, lint and Error-Prone. Run `./scripts/ci/ci_check.sh` to execute all checks.
 * Unit-Tests are verified using `./gradlew testDebugUnitTest`. 

## Components
* Main app (AnySoftKeyboard) is located under `ime/`.
    * to build the APK use following command: `gradlew :ime:app:assembleDebug`
* All add-ons are under `addons/`.
    * Language-packs are under `addons/languages/`.
    * Themes are under `addons/themes/`.
    * Quick-Text (e.g., emojis) are under `addons/quicktext/`.
    * Contributing document for add-ons can be found [here](addons/CONTRIBUTING.md).

# License
The components in this repository are released under the [Apache2](https://www.apache.org/licenses/LICENSE-2.0) license.
By contributing to this repository you give all copyright and distribution rights or AnySoftKeyboard maintainer.<br/>
Of course, since this is Apache2, you may fork and do whatever you want with the code, you do not have to share back only give attribute.<br/>
Read more about this license [here](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)).

# Contributors
If you decided to help out and contribute your time, we are greatly appreciate this.
Our contributors list can be found [here](CONTRIBUTORS.md).
Contributors should adhere to the (Code-of-Conduct)[CODE_OF_CONDUCT.md] document.
