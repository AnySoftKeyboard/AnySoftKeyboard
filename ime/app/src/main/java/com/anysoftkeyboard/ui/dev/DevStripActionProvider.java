package com.anysoftkeyboard.ui.dev;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

public class DevStripActionProvider implements KeyboardViewContainerView.StripActionProvider {
    @NonNull private final Context mContext;

    public DevStripActionProvider(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public @NonNull View inflateActionView(@NonNull ViewGroup parent) {
        View root = LayoutInflater.from(mContext).inflate(R.layout.dev_tools_action, parent, false);
        root.setOnClickListener(v -> startDevToolsFragment());
        return root;
    }

    private void startDevToolsFragment() {
        Intent devTools =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(mContext.getString(R.string.deeplink_url_dev_tools)),
                        mContext,
                        MainSettingsActivity.class);
        devTools.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(devTools);
    }

    @Override
    public void onRemoved() {}
}
