AnySoftKeyboard
====================
`master` latest build: ![](https://github.com/AnySoftKeyboard/AnySoftKeyboard/workflows/AnySoftKeyboard/badge.svg)<br/>
`master` coverage: [![codecov](https://codecov.io/gh/AnySoftKeyboard/AnySoftKeyboard/branch/master/graph/badge.svg)](https://codecov.io/gh/AnySoftKeyboard/AnySoftKeyboard)<br/>
<br/>
&nbsp;&nbsp;&nbsp;&nbsp;[![Gitter chat](https://badges.gitter.im/AnySoftKeyboard/gitter.png)](https://gitter.im/AnySoftKeyboard)


Android (f/w 2.1+, API level 7+) on screen keyboard for multiple languages.

<a href="https://play.google.com/store/apps/details?id=com.menny.android.anysoftkeyboard&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" height="80pt"/></a>&nbsp;&nbsp;<a href="https://f-droid.org/repository/browse/?fdid=com.menny.android.anysoftkeyboard"><img alt="Get it on F-Droid" src="https://f-droid.org/wiki/images/5/55/F-Droid-button_get-it-on_bigger.png" height="80pt"/></a>

Sign up to beta-channel [here](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard). Beta versions are pushed via Google Play Store.

# Features
 * All kinds of keyboards:
     * supporting lots of languages via external packages. E.g., English (QWERTY, Dvorak, AZERTY, Colemak, and Workman), Hebrew, Russian, Arabic, Lao, Bulgarian, Swiss, German, Swedish, Spanish, Catalan, Belarusian, Portuguese, Ukrainian and many more.
     * Special keyboard for text fields which require only numbers.
     * Special keyboard for text fields which require email or URI addresses.
 * Physical keyboard is supported as-well.
 * Auto capitalization.
 * Word suggestions, and Next-Word suggestions.
 * Special key-press effects:
     * Sound on key press (if phone is not muted).
     * Vibrate on key press.
 * Voice input. 
 * Incognito Mode - will not learn new words, will not keep history of what was typed (including emoji history).
 * Plenty of emojis - long-press the smiley key. You customize those by clicking the Settings icon in emojis window.
 * More on AnySoftKeyboard can be found [here](http://anysoftkeyboard.github.io/)

# Release
* Every commit to _master_ branch will deploy a new release to the _ALPHA_ channel in Google Play-Store. You can subscribe to this release channel by joining the [Google+](https://plus.google.com/communities/106050362696748221135) group, and opt-in by visiting [this link](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard).
* Every once in a while, a stable enough _ALPHA_ will be promoted to _BETA_. You can opt-in to this channel by visiting [this link](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard).
* Once all requirements for a release were finished, a _STABLE_ release branch (in the format of `release-branch-vX.X-rX`) will be cut. Every commit to this branch will be automatically published to Google Play Store, and will roll-out users gradually.

# Development
To develop this project, you'll need:
 * Java 9 - get [OpenJDK](https://jdk.java.net/archive/) or [AdoptOpenJDK](https://adoptopenjdk.net/releases.html?variant=openjdk9&jvmVariant=hotspot).
 * git
 * [Android Studio](https://developer.android.com/studio/install)
 * Android SDK API Level 28 installed. Set up `local.properties` file to point to it (that is, ensure you have `sdk.dir=/path/to/android/sdk` in it).
 * Android NDK [r14b](https://developer.android.com/ndk/downloads/older_releases.html) installed. Set up `local.properties` file to point to it (that is, ensure you have `ndk.dir=/path/to/android/ndk` in it).

## Continuous Integration and Pull-Requests Verifications
Each PR is verified by multiple tasks:
 * Code Style is verified by _Google Java Format_. You can auto-format changes by running `./gradlew googleJavaFormat`
 * Static-Analysis is done with PMD, checkstyle, lint and Error-Prone. Run `./scripts/ci/ci_check.sh` to execute all checks.
 * Unit-Tests are verified using `./gradlew testDebugUnitTest`. 
 
# Notes

* Via [Bountysource](https://www.bountysource.com/teams/anysoftkeyboard/issues), users can post bountys and developers can claim them when fixing bugs. This is an external community project and not directly sponsored by the ASK developers!


License
-------

    Copyright 2018 Menny Even-Danan
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
