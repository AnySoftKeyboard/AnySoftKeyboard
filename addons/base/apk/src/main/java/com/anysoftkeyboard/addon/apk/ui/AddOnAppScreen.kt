package com.anysoftkeyboard.addon.apk.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anysoftkeyboard.addon.base.apk.R

@Composable
fun AddOnAppScreen(
    @StringRes addOnName: Int,
    @StringRes addOnDescription: Int,
    @StringRes addOnWebsite: Int,
    @StringRes addOnReleaseNotes: Int,
    @DrawableRes screenshot: Int,
    version: String,
    isAskInstalled: Boolean,
    onActionClick: () -> Unit,
    onHideLauncherClick: () -> Unit,
    onWebsiteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .verticalScroll(scrollState)
              .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
        text = stringResource(R.string.welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(R.string.welcome_subtitle_template, stringResource(addOnName)),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    val websiteUrl = stringResource(addOnWebsite)
    Text(
        text = stringResource(R.string.add_on_website_template, websiteUrl),
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
        textAlign = TextAlign.Center,
        modifier = Modifier.clickable { onWebsiteClick(websiteUrl) },
    )

    Image(
        painter = painterResource(id = screenshot),
        contentDescription = stringResource(R.string.screenshot_content_description),
        modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Inside,
    )

    Text(
        text = stringResource(addOnDescription),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )

    Text(
        text = stringResource(R.string.not_standalone_app_text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text =
            stringResource(
                if (isAskInstalled) {
                  R.string.ask_installed
                } else {
                  R.string.ask_is_missing_need_install
                },
            ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )

    Button(
        onClick = onActionClick,
        modifier = Modifier.fillMaxWidth(0.85f),
    ) {
      Text(
          text =
              stringResource(
                  if (isAskInstalled) {
                    R.string.open_ask_main_settings
                  } else {
                    R.string.open_ask_in_vending
                  },
              ),
          style = MaterialTheme.typography.labelLarge,
      )
    }

    OutlinedButton(
        onClick = onHideLauncherClick,
        modifier = Modifier.fillMaxWidth(0.85f),
    ) {
      Text(
          text = stringResource(R.string.hide_launcher_icon_action),
          style = MaterialTheme.typography.labelLarge,
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(modifier = Modifier.fillMaxWidth(0.9f))

    Text(
        text =
            stringResource(
                R.string.release_notes_template,
                version,
                stringResource(addOnReleaseNotes),
            ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
  }
}
