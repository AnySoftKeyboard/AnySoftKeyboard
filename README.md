# AnySoftKeyboard-API
API library for AnySoftKeyboard.

You need this api library, if you are going to implement a keyboard layout, theme, addon, etc.

### Dependency ###
The library is served by `jcenter`. Ensure you have that maven provider:

Add it in your build.gradle at the end of repositories:
```
repositories {
    //...
    jcenter()
}
```
Step 2. Add the dependency in the form
```
dependencies {
    compile 'com.anysoftkeyboard.api:api:1.11.+'
}
```
Add this to the list of dependencies at the bottom of the app-level `build.gradle`. See [this](https://github.com/AnySoftKeyboard/AnySoftKeyboard/blob/master/build.gradle) or [this](https://github.com/AnySoftKeyboard/LanguagePack/blob/master/build.gradle) for examples.
