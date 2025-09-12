# AnySoftKeyboard&nbsp;[![Latest release](https://img.shields.io/github/release/AnySoftKeyboard/AnySoftKeyboard.svg)](https://github.com/AnySoftKeyboard/AnySoftKeyboard/releases)

Discussions can be found on&nbsp;[💬&nbsp;GitHub](https://github.com/AnySoftKeyboard/AnySoftKeyboard/discussions)<br/>
Follow us on <a rel="me" href="https://hachyderm.io/@anysoftkeyboard">Mastodon</a>
<br/>
`main` latest build&nbsp;[![checks](https://github.com/AnySoftKeyboard/AnySoftKeyboard/actions/workflows/checks.yml/badge.svg)](https://github.com/AnySoftKeyboard/AnySoftKeyboard/actions/workflows/checks.yml)<br/>
`main` coverage&nbsp;[![codecov](https://codecov.io/gh/AnySoftKeyboard/AnySoftKeyboard/branch/main/graph/badge.svg)](https://codecov.io/gh/AnySoftKeyboard/AnySoftKeyboard)<br/>
<br/>
Android (f/w 4.0.3+, API level 15+) on screen keyboard for multiple languages.

<a href="https://play.google.com/store/apps/details?id=com.menny.android.anysoftkeyboard&utm_source=global_co&utm_medium=prtnr&utm_content=Mar2515&utm_campaign=PartBadge&pcampaignid=MKT-AC-global-none-all-co-pr-py-PartBadges-Oct1515-1"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" height="80pt"/></a>&nbsp;&nbsp;<a href="https://f-droid.org/repository/browse/?fdid=com.menny.android.anysoftkeyboard"><img alt="Get it on F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80pt"/></a>

Sign up to beta-channel [here](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard). Beta versions are pushed via Google Play Store.

## Features

- All kinds of keyboards:
  - Supporting lots of languages via external packages. E.g., English (QWERTY, Dvorak, AZERTY, Colemak, and Workman), Hebrew, Russian, Arabic, Lao, Bulgarian, Swiss, German, Swedish, Spanish, Catalan, Belarusian, Portuguese, Ukrainian and [many more](addons/languages/PACKS.md).
  - Special keyboard for text fields which require only numbers.
  - Special keyboard for text fields which require email or URI addresses.
- Physical keyboard is supported as-well.
- Auto-capitalization.
- Word suggestions, and Next-Word suggestions.
  - Automatic correction can be customized, or turned off entirely.
  - External packages include word lists that can be freely mixed. You can use a French layout and get suggestions for German and Russian!
- Gesture typing.
- Dark mode, automatic (based on system) and manual.
- Power saving mode, disables various features to save battery.
- Per-app tint, the keyboard changes color depending on the current app.
- Special key-press effects:
  - Sound on key press (if phone is not muted).
  - Vibrate on key press.
- Voice input.
- Incognito Mode - will not learn new words, will not keep history of what was typed (including emoji history).
- Plenty of emojis - long-press the smiley key. You customize those by clicking the Settings icon in emojis window.
- More on AnySoftKeyboard can be found [here](https://anysoftkeyboard.github.io/).

## Releases

### from _main_ branch

#### IME - AnySoftKeyboard

- Every commit to _main_ branch will [deploy](.github/workflows/checks.yml) a new release to the _ALPHA_ channel in Google Play-Store. You can subscribe to this release channel by joining the [Google Groups](https://groups.google.com/d/forum/anysoftkeyboard-alpha-testers) group, and opt-in by visiting [this link](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard).
- Every Wednesday the latest _ALPHA_ will be [promoted](.github/workflows/deployment_promote.yml) to _BETA_. You can opt-in to this channel by visiting [this link](https://play.google.com/apps/testing/com.menny.android.anysoftkeyboard).
- Once all requirements for a release were finished, a _STABLE_ release branch (in the format of `release-branch-ime-vX.X-rX`) will be cut. Every commit to this branch will be automatically published to Google Play Store (_STABLE_ channel) and will roll-out users gradually.

#### AddOns

- Every commit to _main_ branch will [deploy](.github/workflows/checks.yml) all addons to closed-testing _ALPHA_ channel in Google Play-Store. If you want to subscribe to this channel, open a [ticket](https://github.com/AnySoftKeyboard/AnySoftKeyboard/discussions/3391) and asking to join.
- Every Wednesday the latest _ALPHA_ will be promoted to _BETA_. This is only for supported packs. The _BETA_ channel is an open-testing group, you can subscribe to this release channel by joining the _BETA_ channel for each [addon](addons/languages/PACKS.md).
- Once all requirements for a release were finished, a _STABLE_ release branch (in the format of `release-branch-addons-vX.X-rX`) will be cut. Every commit to this branch will be automatically published to Google Play Store (_STABLE_ channel) and will roll-out users gradually.

### from _release-branch_

#### IME - AnySoftKeyboard

- Each new commit to the release-branch will be published to 10% of the users.
- Each day - if no new commit was pushed to the release-branch - we will increase the roll-out.
- When roll-out reaches 100%, an fdroid release will be made.

#### AddOns

- Each new commit to the release-branch will be published to 10% of the users.
- Each day - if no new commit was pushed to the release-branch - we will increase the roll-out.

## Read more

- Our fancy [web-site](https://anysoftkeyboard.github.io/)
- [Language-Pack](addons/languages/PACKS.md) add-ons in this repo.
- [Theme](addons/themes/PACKS.md) add-ons in this repo.
- [Quick-Text](addons/quicktexts/PACKS.md) add-ons in this repo.
- [Crowdin](https://crowdin.com/project/anysoftkeyboard) to translate the app to your language. [![Crowdin](https://badges.crowdin.net/anysoftkeyboard/localized.svg)](https://crowdin.com/project/anysoftkeyboard)

# Development/Contributing

Want to develop a new feature, fix a bug, or add new language-pack? Read more [here](CONTRIBUTING.md).
Contributors should adhere to the [Code of Conduct](CODE_OF_CONDUCT.md) document.

## Copyright requirement

_Remember:_ the components in this repository are released under the Apache2 license. By contributing to this repository you give all copyright and distribution rights to the [AnySoftKeyboard maintainer](https://github.com/menny).

# License

    Copyright 2009 Menny Even-Danan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
