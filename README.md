# AnySoftKeyboard Language Packs
`master` latest build: [![CircleCI](https://circleci.com/gh/AnySoftKeyboard/LanguagePack/tree/master.svg?style=svg)](https://circleci.com/gh/AnySoftKeyboard/LanguagePack/tree/master)<br/>
<br/>
&nbsp;&nbsp;&nbsp;&nbsp;[![Gitter chat](https://badges.gitter.im/AnySoftKeyboard/gitter.png)](https://gitter.im/AnySoftKeyboard)

This repo holds all the official language packs for AnySoftKeyboard (in a mono-repo style).
Each language is a set of two modules, `pack` and `apk`. `pack` is an Android library that holds all the information about the language pack (keyboards, dictionaries, receivers, etc.),
while `apk` bundles that into an installable (and publishable) APK.

**NOTE**: this repo is in transition to mono-repo style. Some language-packs are in _master_ while others are still in the old structure where each language-pack is a branch.

## How to create a Language-Pack

Let's say you want to create a language-pack for Klingon. We'll call the pack `klingon`, and its locale is `kl`.
1. Easiest way to start, is to duplicate one of the other packs: `cp -R english klingon`.
1. Add the new modules to Gradle. Edit `settings.gradle`, add the line:
```
include ":languages:klingon:pack", ":languages:klingon:apk"
```
1. At this point, you'll need to sync you Android Studio project.
1. Rename the package names (`com.anysoftkeyboard.english.*` -> `com.anysoftkeyboard.klingon.*`:
    * Change `package` value in `AndroidManifest.xml` files (one in `pack` and the second in `apk`).
    * Move the `PackBroadcastReceiver` package to the right place.
1. Rename the `english_keyboards.xml` and `english_dictionary.xml` files (under `klingon/pack/src/main/res/xml`) to `klingon_keyboards.xml` and `klingon_dictionaries.xml`.
1. Replace the dictionary files under `klingon/pack/dictionary` with inputs matching your language (and remove what's not relevant):
    * Try to locate AOSP dictionary files (could be found at [AOSP](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/master/dictionaries/), or [LineageOS](https://github.com/LineageOS/android_packages_inputmethods_LatinIME/tree/lineage-16.0/dictionaries)).
    * If you have anything that was pre-built into a word-list XML, put those under `klingon/pack/dictionary/prebuilt`.
    * Add text files that will be parsed - word-counted -  to generate word-list XMLs
1. Generate the dictionary: `./gradlew :languages:kligon:pack:makeDictionary`. This will create the following files (which _should not_ checked into the repo):
    * raw resources under `klingon/pack/src/main/res/raw/klingon_words_?.dict`
    * IDs resource array under `klingon/pack/src/main/res/values/klingon_words_dict_array.xml`
1. Edit `klingon_dictionaries.xml`:
    * to point to the new array resource `@array/klingon_words_dict_array`
    * replace the `id` with a new [GUID](https://www.guidgenerator.com/).
    * replace name and description
    * replace the locale
1. Set the status-bar icon text at `klingon/pack/build.gradle`: `ext.status_icon_text = "kl"`
1. Replace the flag at klingon/apk/flag` with a, high-quality, png or svg, image of the flag. It should be named `flag.png` or `flag.svg`. _Note_ that sometimes svg files are not converted correctly, but they will produce highest quality if successful.
1. To generate the icons, you'll need ImageMagick installed on your path. Check out the installation [page](https://imagemagick.org/script/download.php) for details.
1. Generate the icons: `./gradlew :languages:kligon:pack:generateLanguagePackIcons :languages:kligon:apk:generateLanguagePackIcons`. This will generate the following files (which _should_ be checked into the repo):
    * `klingon/pack/src/main/res/drawable-*/ic_status_kligon.png`
    * `klingon/apk/src/main/res/mipmap-*/ic_launcher.png`
    * `klingon/apk/src/main/play/listings/en-US/graphics/feature-graphic/pack_store_feature_graphics.png`
    * `klingon/apk/src/main/play/listings/en-US/graphics/icon/pack_store_icon.png`
1. Edit the keyboard layouts to your language, remove what's not needed, and add new ones if you need. Make sure the names of the layouts include you pack-name (klingon).
1. Edit `klingon_keyboards.xml`. For each of the layouts you want in your pack (the layouts you created in the previous step):
    * Generate a new [GUID](https://www.guidgenerator.com/).
    * Set the locale value at `defaultDictionaryLocale` to match the value you used in `klingon_dictionaries.xml`.
    * Set the status-bar icon at `iconResId` to the generate icon `@drawable/ic_status_kl`
    * Update the texts (name and description).

At this point, you should be able to build an APK that can be installed on your device:
```
./gradlew :languages:klingon:apk:assembleDebug
```
or directly install it on your connected device:
```
./gradlew :languages:klingon:apk:installDebug
```


Iterate on your pack until you feel it is good, and then create a PR to merge it to the _master_.

## Publish pack
You can either publish by yourself, under your developer account and keep complete ownership, or you can let us (aka AnySoftKeyboard organization) do it.

### Play Store Publish by AnySoftKeyboard organization
TO DO!!!

## Languages in this repo
We have 6 language packs in this repo:

* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/afrikaans_oss/apk/flag/flag.png' height='16'>&nbsp;Afrikaans_oss: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/afrikaans_oss) • [Play Store](https://play.google.com/store/apps/details?id=com.anysoftkeyboard.languagepack.afrikaans_oss)
* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/armenian2/apk/flag/flag.png' height='16'>&nbsp;Armenian2: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/armenian2)
* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/english/apk/flag/flag.png' height='16'>&nbsp;English: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/english)
* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/hebrew/apk/flag/flag.png' height='16'>&nbsp;Hebrew: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/hebrew) • [Play Store](https://play.google.com/store/apps/details?id=com.anysoftkeyboard.languagepack.hebrew)
* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/italian/apk/flag/flag.png' height='16'>&nbsp;Italian: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/italian) • [Play Store](https://play.google.com/store/apps/details?id=com.anysoftkeyboard.languagepack.italian)
* <img src='https://github.com/AnySoftKeyboard/LanguagePack/raw/master/languages/russian2/apk/flag/flag.png' height='16'>&nbsp;Russian2: [Source](https://github.com/AnySoftKeyboard/LanguagePack/tree/master/languages/russian2) • [Play Store](https://play.google.com/store/apps/details?id=com.anysoftkeyboard.languagepack.russian2)

# License

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


# OLD - to be removed!

This is the common template for language Packs for the [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard) app for Android devices.
Each pack can contain and provide multiple keyboards or dictionaries for auto correction.
Most packs are maintained here as [branches of the repository](https://github.com/AnySoftKeyboard/LanguagePack/branches) and published to Google Play Store and F-Droid repository. There are some packs maintained as community forks, here on GitHub or not open source at all. Some of these are:

* [NEO2](https://github.com/kertase/neo_anysoftkeyboard)
* [Magyar (Hungarian)](https://github.com/rhornig/anysoftkeyboard-hungarian)
* [Swiss](https://play.google.com/store/apps/details?id=ch.masshardt.anysoftkeyboard.swisslanguagepack)
* [Pali (Indian)](https://github.com/yuttadhammo/ask-pali-keyboard)
* [Malayalam, Kannada and other Indian](https://sriandroid.wordpress.com/)
* [SSH](https://github.com/pi3ch/ssh_anysoftkeyboard)
* [Dutch](https://github.com/OpenTaal/LanguagePack/tree/Dutch)

To start a new pack, follow this checklist:

1. Fork this repository.
1. Create a new branch, name it after the language.
1. In Android Studio, Refactor->Rename the folder/package com.anysoftkeyboard.languagepack.languagepack in the project tree, replacing the last `languagepack` with the name of the language. This will automatically change it at a couple of other locations.
1. Change `applicationId` in `build.gradle` in the same way.
1. Edit `src/main/res/xml/keyboards.xml` according to the comments in it. It references `src/main/res/xml/qwerty.xml`, so edit this as well. Have a look at all the other Language Pack branches, to get an idea, what is possible and how to correctly configure a keyboard.
1. If you want to add more keyboards, you can do this by copying `qwerty.xml` and add a <keyboard> element in `keyboards.xml`. The user can pre-select in the ASK settings, which keyboards she would like to have available for toggling through.
1. Edit `src/main/res/xml/dictionaries.xml`
1. Edit `src/main/res/values/strings.xml`, change the strings there and possibly add some more which are referenced in the other xml files. Also, create a new folder `src/main/res/values-XX`, where `XX` is the correspondent two-letter ISO 639-1 language code.
1. Edit (e.g. via Inkscape) one of the files in `src/main/svg-png/flag/` to represent the language, e.g. by using the flag from Wikipedia (the flag has to be placed on the right edge of the document and have the full height).
1. Rebuild the drawables with `./gradlew svgToDrawablePng` or "Build" -> "Rebuild Project" in Android Studio. Drawables will be generated at `src/main/res/mipmap-*/`.
1. Choose whether you like the standard or the broad variant and set that as application's `android:icon` in `src/main/AndroidManifest.xml`.
1. You can also add a new `src/main/res/drawable/flag.png` and reference it in the `iconResId=""` attribute in the keyboards.xml.
1. Put the source files for the dictionary into the dictionary/ directory. Take special care to take the conditions of the license into account, under which you obtained the data.
1. Change the build.gradle to use and configure the tasks necessary. There are several different variants ([more Information](https://github.com/AnySoftKeyboard/AnySoftKeyboardTools/blob/master/README.md)):
    * `GenerateWordsListTask`
    * `GenerateWordsListFromAOSPTask`
    * `MergeWordsListTask`
1. Change the README.md to reflect the characteristics of your pack
1. Make some screenshots and replace the files in the `src/main/play/` folder. One of them should be a 1024x500 banner.
1. If a branch of the language does not exist, [open an issue](https://github.com/AnySoftKeyboard/LanguagePack/issues/new) to request the creation of a new branch. As soon, as it is created, you can make a Pull Request from your forked branch to the one in the original repository. Provide translations of the following strings to your language:
    * title: "LANGUAGE Language Pack"
    * promo: "LANGUAGE language pack for AnySoftKeyboard"
    * description: "AnySoftKeyboard keyboards pack:
      LANGUAGE keyboard and dictionary.

      This is an expansion pack for AnySoftKeyboard.
      Install AnySoftKeyboard first, and then select the desired layout from AnySoftKeyboard's Settings->Keyboards menu."

    When it is merged, it can take a couple of days, until it is also distributed via Play Store and F-Droid.