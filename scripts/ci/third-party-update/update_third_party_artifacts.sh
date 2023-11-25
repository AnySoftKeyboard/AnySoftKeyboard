#!/usr/bin/env bash
set -e

GREP_VERSION_CLASSES="[[:digit:]]\\{1,\\}[.][[:digit:]]\\{1,\\}[[:punct:][:alnum:]]*"

if [[ -z "$(git config --list | grep user.name)" ]]; then
  git config user.email "ask@evendanan.net"
  git config user.name "Polyglot"
fi

function do_update() {
  local MAVEN_URL="$1"
  local GROUP_ID="$2"
  local ARTIFACT_ID="$3"

  local LATEST_VERSION
  LATEST_VERSION=$(./scripts/ci/third-party-update/get_latest_maven_artifact_version.sh "${MAVEN_URL}" "${GROUP_ID}" "${ARTIFACT_ID}")
  if [[ -z "$LATEST_VERSION" ]]; then
    echo "Failed to load versions metadata for '${GROUP_ID}:${ARTIFACT_ID}'."
  else
    echo -n "Found version '${LATEST_VERSION}' for '${GROUP_ID}:${ARTIFACT_ID}'. Checking if bump is needed..."
    for f in $(find . -name 'build.gradle'); do
      sed "s/${GROUP_ID}:${ARTIFACT_ID}:${GREP_VERSION_CLASSES}'/${GROUP_ID}:${ARTIFACT_ID}:${LATEST_VERSION}'/g" "$f" > /tmp/output.file
      cp /tmp/output.file "$f"
    done

    if [[ -n $(git status -s) ]]; then
      echo " Bumped."
      git add .
      git commit -m "Bumping $4 to version ${LATEST_VERSION} from ${MAVEN_URL}."
    else
      echo " Already on latest."
    fi
  fi
}

do_update https://dl.google.com/dl/android/maven2 com.android.tools.build gradle "Android Gradle Plugin"

#support-lib
do_update https://dl.google.com/dl/android/maven2 androidx.annotation annotation "Android Annotations Compat Library"
do_update https://dl.google.com/dl/android/maven2 androidx.core core "AndroidX core library"
do_update https://dl.google.com/dl/android/maven2 androidx.navigation navigation-safe-args-gradle-plugin "Navigation Safe Args"
do_update https://dl.google.com/dl/android/maven2 androidx.navigation navigation-fragment "Navigation"
do_update https://dl.google.com/dl/android/maven2 androidx.navigation navigation-ui "Navigation"
do_update https://dl.google.com/dl/android/maven2 androidx.navigation navigation-dynamic-features-fragment "Navigation"
do_update https://dl.google.com/dl/android/maven2 androidx.test core "Test Support library"
do_update https://dl.google.com/dl/android/maven2 androidx.test.ext junit "JUnit Test Support library"
do_update https://dl.google.com/dl/android/maven2 androidx.autofill autofill "Form Auto-Fill Support library"
do_update https://dl.google.com/dl/android/maven2 androidx.multidex multidex "App Multi-Dex Support library"
do_update https://dl.google.com/dl/android/maven2 androidx.appcompat appcompat "App Compat library"
do_update https://dl.google.com/dl/android/maven2 androidx.fragment fragment "Fragment Support library"
do_update https://dl.google.com/dl/android/maven2 androidx.recyclerview recyclerview "RecyclerView library"
do_update https://dl.google.com/dl/android/maven2 androidx.cardview cardview "CardView library"
do_update https://dl.google.com/dl/android/maven2 androidx.palette palette "Palette library"
do_update https://dl.google.com/dl/android/maven2 com.google.android.material material "Material Design library"
do_update https://dl.google.com/dl/android/maven2 androidx.preference preference "Prefernce Storage library"
do_update https://dl.google.com/dl/android/maven2 androidx.legacy legacy-support-v13 "Legacy v13 support library"
do_update https://dl.google.com/dl/android/maven2 androidx.legacy legacy-support-v4 "Legacy v4 support library"
do_update https://dl.google.com/dl/android/maven2 androidx.legacy legacy-support-core-utils "Legacy core utils support library"
do_update https://dl.google.com/dl/android/maven2 ndroidx.viewpager2 view-viewpager2 "View Pager v2"

#kotlin
do_update https://dl.google.com/dl/android/maven2 androidx.core core-ktx "Kotlin core extensions"

#compose
do_update https://dl.google.com/dl/android/maven2 androidx.compose.material material "Kotlin Compose Material"
do_update https://dl.google.com/dl/android/maven2 androidx.activity activity-compose "Kotlin Compose Activity"
do_update https://dl.google.com/dl/android/maven2 androidx.compose.animation animation "Kotlin Compose Animation"
do_update https://dl.google.com/dl/android/maven2 androidx.compose.ui ui-tooling "Compose UI tools"
do_update https://dl.google.com/dl/android/maven2 androidx.lifecycle lifecycle-viewmodel-compose "Compose UI lifecycle"

#rx
do_update https://repo1.maven.org/maven2 io.reactivex.rxjava2 rxandroid "RXJava for Android"
do_update https://repo1.maven.org/maven2 io.reactivex.rxjava2 rxjava "RXJava"
do_update https://repo1.maven.org/maven2 com.github.karczews rx2-broadcast-receiver "Broadcast Receiver for RXJava"
do_update https://repo1.maven.org/maven2 app.cash.copper copper-rx2 "Copper for RXJava"
do_update https://repo1.maven.org/maven2 com.f2prateek.rx.preferences2 rx-preferences "Preferences as RX library"

#others
do_update https://repo1.maven.org/maven2 com.jpardogo.materialtabstrip library "Material Tab Strip library"
do_update https://repo1.maven.org/maven2 pub.devrel easypermissions "Easy-Permissions support library"
do_update https://repo1.maven.org/maven2 com.getkeepsafe.relinker relinker "ReLinker JNI support library"
