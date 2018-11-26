# AnySoftKeyboard-API
API library for AnySoftKeyboard.
<br/>
[![CircleCI](https://circleci.com/gh/AnySoftKeyboard/AnySoftKeyboard-API.svg?style=svg)](https://circleci.com/gh/AnySoftKeyboard/AnySoftKeyboard-API)
<br/>
You need this api library if you are going to implement a keyboard layout, theme, language-pack or any other addon for AnySoftKeyboard.

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
    implementation 'com.anysoftkeyboard.api:api:1.11.+'
}
```

