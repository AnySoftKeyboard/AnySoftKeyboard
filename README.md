This is the common template for language Packs for the [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard) app for Android devices.
Each pack can contain and provide multiple keyboards or dictionaries for auto correction.
Most packs are maintained here as [branches of the repository](https://github.com/AnySoftKeyboard/LanguagePack/branches) and published to Google Play Store and F-Droid repository. There are some packs maintained as community forks, here on GitHub or not open source at all. Some of these are:

* [NEO2](https://github.com/kertase/neo_anysoftkeyboard)
* [Magyar (Hungarian)](https://github.com/rhornig/anysoftkeyboard-hungarian)
* [Swiss](https://play.google.com/store/apps/details?id=ch.masshardt.anysoftkeyboard.swisslanguagepack)
* [Pali (Indian)](https://github.com/yuttadhammo/ask-pali-keyboard)
* [Malayalam, Kannada and other Indian](https://sriandroid.wordpress.com/)

To start a new pack, follow this checklist:

1. Fork this repository.
2. Create a new branch, name it after the language.
3. In Android Studio, Refactor->Rename the folder/package com.anysoftkeyboard.languagepack.languagepack in the project tree, replacing the last `languagepack` with the name of the language. This will automatically change it at a couple of other locations.
4. Change `applicationId` in `build.gradle` in the same way.
5. Edit `src/main/res/xml/keyboards.xml` according to the comments in it. It references `src/main/res/xml/qwerty.xml`, so edit this as well. Have a look at all the other Language Pack branches, to get an idea, what is possible and how to correctly configure a keyboard.
6. If you want to add more keyboards, you can do this by copying `qwerty.xml` and add a <keyboard> element in `keyboards.xml`. The user can pre-select in the ASK settings, which keyboards she would like to have available for toggling through.
7. Edit `src/main/res/xml/dictionaries.xml`
8. Edit `src/main/res/values/strings.xml`, change the strings there and possibly add some more which are referenced in the other xml files. Also, create a new folder `src/main/res/values-XX`, where `XX` is the correspondent two-letter ISO 639-1 language code.
9. Edit `src/main/res/drawable/app_icon.png` to represent the language, e.g. by adding a flag. You can also add a new flag.png and reference it in the `iconResId=""` attribute in the keyboards.xml.
10. Put the source files for the dictionary into the dictionary/ directory. Take special care to take the conditions of the license into account, under which you obtained the data.
11. Change the build.gradle to use and configure the tasks necessary. There are several different variants ([more Information](https://github.com/AnySoftKeyboard/AnySoftKeyboardTools/blob/master/README.md)):
    * `GenerateWordsListTask`
    * `GenerateWordsListFromAOSPTask`
    * `MergeWordsListTask`
12. Change the README.md to reflect the characteristics of your pack
13. Make some screenshots and replace the files in the StoreStuff/ folder. One of them should be a 1024x500 banner.
14. If a branch of the language does not exist, [open an issue](https://github.com/AnySoftKeyboard/LanguagePack/issues/new) to request the creation of a new branch. As soon, as it is created, you can make a Pull Request from your forked branch to the one in the original repository. Provide translations of the following strings to your language:
    * title: "LANGUAGE Language Pack"
    * promo: "LANGUAGE language pack for AnySoftKeyboard"
    * description: "AnySoftKeyboard keyboards pack:
      LANGUAGE keyboard and dictionary.

      This is an expansion pack for AnySoftKeyboard.
      Install AnySoftKeyboard first, and then select the desired layout from AnySoftKeyboard's Settings->Keyboards menu."

    When it is merged, it can take a couple of days, until it is also distributed via Play Store and F-Droid.