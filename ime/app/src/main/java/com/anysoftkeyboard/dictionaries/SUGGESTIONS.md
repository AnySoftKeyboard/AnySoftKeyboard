# AnySoftKeyboard Suggestions Flow

This document outlines the high-level architecture, flows, and state evolution of word suggestion in AnySoftKeyboard.

## High-Level Description

Suggestions in AnySoftKeyboard are provided by a combination of dictionary sources configured dynamically by the user and the keyboard environment. The `SuggestImpl` orchestrates querying these dictionaries for matching strings given the user's typed `WordComposer` (a sequence of key codes, possibly fuzzy). `SuggestionsProvider` acts as the manager of various `Dictionary` instances (like Main, User, Contacts, AutoText, and Abbreviations) and aggregates their results.

## Key Classes and Interfaces

- [`Suggest.java`](./Suggest.java) - The core interface for components providing keyboard suggestions.
- [`SuggestImpl.java`](./SuggestImpl.java) - The concrete implementation of `Suggest`. Handles string buffering, basic capitalizations, auto-text, abbreviations, fuzzy matching (edit distance), and splitting logic via various `Dictionary.WordCallback`s.
- [`SuggestionsProvider.java`](./SuggestionsProvider.java) - Manages the lifecycle of multiple dictionaries (Main, User, Contacts, AutoText, etc.) and routes queries from `SuggestImpl` to the loaded dictionaries.
- [`Dictionary.java`](../../../../../../dictionaries/src/main/java/com/anysoftkeyboard/dictionaries/Dictionary.java) - Abstract base class representing a searchable list of words. Defines `WordCallback`.
- [`WordComposer.java`](../../../../../../dictionaries/src/main/java/com/anysoftkeyboard/dictionaries/WordComposer.java) - Collects typed characters, key coordinates, and potential fuzzy matches to query dictionaries.

## Flows

### 1. Dictionary Initialization Flow

1. The keyboard/service initializes `SuggestionsProvider`.
2. `SuggestionsProvider` sets up dictionaries using `setupSuggestionsForKeyboard(...)`, receiving a list of `DictionaryAddOnAndBuilder` instances based on user settings (e.g., active languages).
3. Main dictionaries, User dictionaries (based on locale), Contacts dictionary (if permitted), and AutoText/Abbreviations are instantiated and loaded asynchronously using `DictionaryBackgroundLoader`.

### 2. Suggestion Generation Flow (`getSuggestions`)

1. User types characters -> Keyboard updates `WordComposer`.
2. Keyboard calls `Suggest.getSuggestions(WordComposer)`.
3. `SuggestImpl` prepares the internal `mSuggestions` list and calls `SuggestionsProvider.getSuggestions(...)`, `SuggestionsProvider.getAbbreviations(...)`, and `SuggestionsProvider.getAutoText(...)`.
4. `SuggestionsProvider` routes the `WordComposer` to each active `Dictionary` (Contacts, User, Main, etc.).
5. Each `Dictionary` compares `WordComposer` combinations with its tree/words.
6. For each matched word, the `Dictionary` invokes a specific `Dictionary.WordCallback` (implemented internally by `SuggestImpl` like `SuggestionCallback`, `DictionarySuggestionCallback`, `AutoTextSuggestionCallback`, etc.).
7. `SuggestImpl`'s callback evaluates the frequency, applies bonuses for capitalizations or exact matches, checks `IMEUtil.editDistance`, and decides whether to insert it into `mSuggestions` (limited to `mPrefMaxSuggestions`).
8. The sorted `mSuggestions` is returned to the keyboard to render in the suggestion strip.

### 3. Next Word Prediction Flow (`getNextSuggestions`)

1. Keyboard identifies a typed word has finished.
2. Keyboard calls `Suggest.getNextSuggestions(previousWord, inAllUpperCaseState)`.
3. `SuggestImpl` delegates to `SuggestionsProvider.getNextWords(...)`.
4. `SuggestionsProvider` queries `NextWordSuggestions` dictionaries (User, Contacts).
5. Output is bounded by `mMaxNextWordSuggestionsCount` and sorted by word usage (`mMinWordUsage`).

## State Definition and Evolution

### `SuggestImpl` State

- **mSuggestions / mNextSuggestions**: The output lists representing the current UI state to display. They are reset/cleared each time `getSuggestions` is called.
- **mStringPool**: Used to minimize allocations when generating strings for suggestions.
- **Capitalization State (`mIsFirstCharCapitalized`, `mIsAllUpperCase`)**: Set during `getSuggestions` based on `WordComposer.isFirstCharCapitalized()` and `WordComposer.isAllUpperCase()`. This state is used in the `WordCallback` to format dictionary words appropriately before insertion into the suggestion list.
- **mCorrectSuggestionIndex**: Tracks which suggestion in `mSuggestions` is considered the primary correction/fix for the typed word.

### Configuration and Environment Flags

The suggestions flow is highly impacted by user configurations (`SharedPreferences`):

- `mPrefMaxSuggestions`: Maximum number of suggestions to generate (configurable).
- `mEnabledSuggestions`: Toggles dictionary corrections globally.
- `mIncognitoMode`: If true, stops adding words to User Dictionary and stops tracking Next-Word patterns.
- `mCommonalityMaxLengthDiff` & `mCommonalityMaxDistance`: Toggles strictness of fuzzy matching.
- `mContactsDictionaryEnabled`: Flag allowing/denying access to Android Contacts for suggestions.
- `mNextWordEnabled`: Enables/disables predicting the next word based on context.
- `mQuickFixesEnabled`: Toggles auto-text replacements and abbreviations.

### Environment, IME Callbacks, and EditorInfo Flags

The suggestion flows are deeply integrated with Android's `InputMethodService` lifecycle callbacks (managed in `AnySoftKeyboardSuggestions.java`).

- **`onStartInputView`**: This is the primary entry point for determining if suggestions should be active for a given input field. It inspects the `EditorInfo` passed by the OS.
  - It initializes the default state (`mPredictionOn = true`).
  - If `EditorInfo.inputType` dictates a non-text field (e.g., `TYPE_CLASS_DATETIME`, `TYPE_CLASS_NUMBER`, `TYPE_CLASS_PHONE`), `mPredictionOn` is forcibly set to `false`.
  - For text fields (`TYPE_CLASS_TEXT`), it checks specific variations:
    - `TYPE_TEXT_VARIATION_PASSWORD`, `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`, and `TYPE_TEXT_VARIATION_WEB_PASSWORD` will disable prediction.
    - `TYPE_TEXT_VARIATION_EMAIL_ADDRESS` and `TYPE_TEXT_VARIATION_URI` disable auto-picking and auto-spacing but may keep predictions on depending on other flags.
  - Explicit developer overrides like `IMEUtil.shouldHonorNoSuggestionsFlag(textFlag)` (which checks for `NO_SUGGESTIONS` or `TYPE_TEXT_FLAG_NO_SUGGESTIONS`) will also set `mPredictionOn = false`.
- **`onUpdateSelection`**: This callback tracks the user's cursor position.
  - If the user manually moves the cursor inside a currently predicted word, the `WordComposer`'s internal cursor position is updated without destroying the state.
  - If the cursor is moved outside the currently predicted word ("no man's land"), the correction state is aborted (`abortCorrectionAndResetPredictionState`) and suggestions are restarted for the new context.
- **Global Overrides**: Finally, `mPredictionOn` is AND'ed with the user preference `mShowSuggestions`. Even if the field supports suggestions, they won't show if the user disabled them globally.

### Reactive Preferences and Dynamic State Evolution

Configuration changes dynamically impact the suggestion engine via RxJava observers.

- **`Observable.combineLatest`**: In `AnySoftKeyboardSuggestions`, multiple reactive streams (shared preferences) are combined to evaluate global conditions:
  - `mShowSuggestions` is bound to the `settings_key_show_suggestions` preference, but it can be immediately disabled if the system enters `PowerSaving` mode. If this value toggles, dictionaries are either initialized (`setDictionariesForCurrentKeyboard()`) or shut down (`closeDictionaries()`).
  - `auto_pick_suggestion_aggressiveness`: This user preference calculates `mAutoComplete` and explicitly updates the fuzzy matching tolerance by calculating and passing `commonalityMaxLengthDiff` and `commonalityMaxDistance` directly to `mSuggest.setCorrectionMode(...)`.
  - `settings_key_try_splitting_words_for_correction`: Also passed down to `setCorrectionMode`, enabling or disabling sub-word splitting logic on the fly.

### PowerSaving Mode and Gesture Typing Impacts

Additional system-level states like `PowerSaving` also intersect with word predictions, particularly through gesture typing mechanisms in `AnySoftKeyboardWithGestureTyping.java`.

- **PowerSaving mode**: When the `settings_key_power_save_mode_gesture_control` is active, another `Observable.combineLatest` reactive stream will intercept the `settings_key_gesture_typing` preference and forcibly set `mGestureTypingEnabled = false`.
- **Gesture Dictionary Bypassing**: When gesture typing is disabled, the system bypasses `WordListDictionaryListener` (which acts as a `DictionaryBackgroundLoader.Listener`). This prevents the `GestureTypingDetector` from parsing the dictionary arrays (`char[][]` paths) to correlate screen swipes with specific dictionary words.
