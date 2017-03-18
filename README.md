# AnySoftKeyboard-API
API library for AnySoftKeyboard.

You need this api library, if you are going to implement a keyboard layout, theme, addon, etc.

### Dependency ###
I'm using [![JitPack](https://img.shields.io/github/release/AnySoftKeyboard/AnySoftKeyboard-API.svg?label=JitPack)](https://jitpack.io/#AnySoftKeyboard/AnySoftKeyboard-API) to publish this library.

Add it in your build.gradle at the end of repositories:
```
repositories {
    //...
    maven { url "https://jitpack.io" }
}
```
Step 2. Add the dependency in the form
```
dependencies {
    compile 'com.github.AnySoftKeyboard:AnySoftKeyboard-API:1.9.0'
}
```
Add this to the list of dependencies at the bottom of the app-level `build.gradle`. See [this](https://github.com/AnySoftKeyboard/AnySoftKeyboard/blob/master/build.gradle) or [this](https://github.com/AnySoftKeyboard/LanguagePack/blob/master/build.gradle) for examples.
