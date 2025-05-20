export type Dep = {
  mavenUrl: string;
  groupId: string;
  artifactId: string;
  friendlyName: string;
};

export const DEPENDENCIES: Dep[] = [
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'com.android.tools.build',
    artifactId: 'gradle',
    friendlyName: 'Android Gradle Plugin',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.annotation',
    artifactId: 'annotation',
    friendlyName: 'Android Annotations Compat Library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.core',
    artifactId: 'core',
    friendlyName: 'AndroidX core library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.navigation',
    artifactId: 'navigation-safe-args-gradle-plugin',
    friendlyName: 'Navigation Safe Args',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.navigation',
    artifactId: 'navigation-fragment',
    friendlyName: 'Navigation',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.navigation',
    artifactId: 'navigation-ui',
    friendlyName: 'Navigation',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.navigation',
    artifactId: 'navigation-dynamic-features-fragment',
    friendlyName: 'Navigation',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.test',
    artifactId: 'core',
    friendlyName: 'Test Support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.test.ext',
    artifactId: 'junit',
    friendlyName: 'JUnit Test Support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.autofill',
    artifactId: 'autofill',
    friendlyName: 'Form Auto-Fill Support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.multidex',
    artifactId: 'multidex',
    friendlyName: 'App Multi-Dex Support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.appcompat',
    artifactId: 'appcompat',
    friendlyName: 'App Compat library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.fragment',
    artifactId: 'fragment',
    friendlyName: 'Fragment Support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.recyclerview',
    artifactId: 'recyclerview',
    friendlyName: 'RecyclerView library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.cardview',
    artifactId: 'cardview',
    friendlyName: 'CardView library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.palette',
    artifactId: 'palette',
    friendlyName: 'Palette library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'com.google.android.material',
    artifactId: 'material',
    friendlyName: 'Material Design library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.preference',
    artifactId: 'preference',
    friendlyName: 'Prefernce Storage library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.legacy',
    artifactId: 'legacy-support-v13',
    friendlyName: 'Legacy v13 support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.legacy',
    artifactId: 'legacy-support-v4',
    friendlyName: 'Legacy v4 support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.legacy',
    artifactId: 'legacy-support-core-utils',
    friendlyName: 'Legacy core utils support library',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.viewpager2',
    artifactId: 'viewpager2',
    friendlyName: 'View Pager v2',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.core',
    artifactId: 'core-ktx',
    friendlyName: 'Kotlin core extensions',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.compose.material',
    artifactId: 'material',
    friendlyName: 'Kotlin Compose Material',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.activity',
    artifactId: 'activity-compose',
    friendlyName: 'Kotlin Compose Activity',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.compose.animation',
    artifactId: 'animation',
    friendlyName: 'Kotlin Compose Animation',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.compose.ui',
    artifactId: 'ui-tooling',
    friendlyName: 'Compose UI tools',
  },
  {
    mavenUrl: 'https://dl.google.com/dl/android/maven2',
    groupId: 'androidx.lifecycle',
    artifactId: 'lifecycle-viewmodel-compose',
    friendlyName: 'Compose UI lifecycle',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'io.reactivex.rxjava2',
    artifactId: 'rxandroid',
    friendlyName: 'RXJava for Android',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'io.reactivex.rxjava2',
    artifactId: 'rxjava',
    friendlyName: 'RXJava',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'com.github.karczews',
    artifactId: 'rx2-broadcast-receiver',
    friendlyName: 'Broadcast Receiver for RXJava',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'app.cash.copper',
    artifactId: 'copper-rx2',
    friendlyName: 'Copper for RXJava',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'com.f2prateek.rx.preferences2',
    artifactId: 'rx-preferences',
    friendlyName: 'Preferences as RX library',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'com.jpardogo.materialtabstrip',
    artifactId: 'library',
    friendlyName: 'Material Tab Strip library',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'pub.devrel',
    artifactId: 'easypermissions',
    friendlyName: 'Easy-Permissions support library',
  },
  {
    mavenUrl: 'https://repo1.maven.org/maven2',
    groupId: 'com.getkeepsafe.relinker',
    artifactId: 'relinker',
    friendlyName: 'ReLinker JNI support library',
  },
];
