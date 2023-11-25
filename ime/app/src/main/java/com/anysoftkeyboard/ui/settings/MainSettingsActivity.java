/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.pixel.EdgeEffectHacker;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class MainSettingsActivity extends AppCompatActivity {

  public static final String ACTION_REQUEST_PERMISSION_ACTIVITY =
      "ACTION_REQUEST_PERMISSION_ACTIVITY";
  public static final String EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY =
      "EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY";

  private CharSequence mTitle;

  /**
   * Will set the title in the hosting Activity's title. Will only set the title if the fragment is
   * hosted by the Activity's manager, and not inner one.
   */
  public static void setActivityTitle(Fragment fragment, CharSequence title) {
    FragmentActivity activity = fragment.requireActivity();
    if (activity.getSupportFragmentManager() == fragment.getParentFragmentManager()) {
      activity.setTitle(title);
    }
  }

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main_ui);

    mTitle = getTitle();

    final NavController navController =
        ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))
            .getNavController();
    final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
    NavigationUI.setupWithNavController(bottomNavigationView, navController);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // applying my very own Edge-Effect color
    EdgeEffectHacker.brandGlowEffect(this, ContextCompat.getColor(this, R.color.app_accent));

    handlePermissionRequest(getIntent());
  }

  private void handlePermissionRequest(Intent intent) {
    if (intent != null
        && ACTION_REQUEST_PERMISSION_ACTIVITY.equals(intent.getAction())
        && intent.hasExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY)) {
      final String permission = intent.getStringExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY);
      intent.removeExtra(EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY);
      if (permission.equals(Manifest.permission.READ_CONTACTS)) {
        startContactsPermissionRequest();
      } else {
        throw new IllegalArgumentException("Unknown permission request " + permission);
      }
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE)
  public void startContactsPermissionRequest() {
    PermissionRequestHelper.check(this, PermissionRequestHelper.CONTACTS_PERMISSION_REQUEST_CODE);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionRequestHelper.onRequestPermissionsResult(
        requestCode, permissions, grantResults, this);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getSupportActionBar().setTitle(mTitle);
  }
}
