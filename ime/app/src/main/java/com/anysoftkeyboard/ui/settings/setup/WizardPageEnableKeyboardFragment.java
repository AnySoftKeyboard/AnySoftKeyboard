package com.anysoftkeyboard.ui.settings.setup;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import app.cash.copper.rx2.RxContentResolver;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class WizardPageEnableKeyboardFragment extends WizardPageBaseFragment {

    private static final int KEY_MESSAGE_UNREGISTER_LISTENER = 447;
    private static final int KEY_MESSAGE_RETURN_TO_APP = 446;

    @SuppressWarnings("HandlerLeak" /*I want this fragment to stay in memory as long as possible*/)
    private final Handler mGetBackHereHandler =
            new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case KEY_MESSAGE_RETURN_TO_APP:
                            if (mReLaunchTaskIntent != null && mBaseContext != null) {
                                mBaseContext.startActivity(mReLaunchTaskIntent);
                                mReLaunchTaskIntent = null;
                            }
                            break;
                        case KEY_MESSAGE_UNREGISTER_LISTENER:
                            unregisterSettingsObserverNow();
                            break;
                        default:
                            super.handleMessage(msg);
                            break;
                    }
                }
            };

    private Context mBaseContext = null;
    private Intent mReLaunchTaskIntent = null;
    @NonNull private Disposable mSecureSettingsChangedDisposable = Disposables.empty();

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_enable_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View.OnClickListener goToDeviceLanguageSettings =
                v -> {
                    // registering for changes, so I'll know to come back here.
                    final Context context = requireContext();
                    mSecureSettingsChangedDisposable =
                            RxContentResolver.observeQuery(
                                            context.getContentResolver(),
                                            Settings.Secure.CONTENT_URI,
                                            null,
                                            null,
                                            null,
                                            null,
                                            true)
                                    .observeOn(RxSchedulers.mainThread())
                                    .forEach(
                                            q -> {
                                                if (!isResumed() && isStepCompleted(context)) {
                                                    // should we return to this task?
                                                    // this happens when the user is asked to enable
                                                    // AnySoftKeyboard, which is
                                                    // done on a different UI activity (outside of
                                                    // my App).
                                                    mGetBackHereHandler.removeMessages(
                                                            KEY_MESSAGE_RETURN_TO_APP);
                                                    mGetBackHereHandler.sendMessageDelayed(
                                                            mGetBackHereHandler.obtainMessage(
                                                                    KEY_MESSAGE_RETURN_TO_APP),
                                                            50 /*enough for the user to see what happened.*/);
                                                }
                                            });
                    // but I don't want to listen for changes for ever!
                    // If the user is taking too long to change one checkbox, I say forget about
                    // it.
                    mGetBackHereHandler.removeMessages(KEY_MESSAGE_UNREGISTER_LISTENER);
                    mGetBackHereHandler.sendMessageDelayed(
                            mGetBackHereHandler.obtainMessage(KEY_MESSAGE_UNREGISTER_LISTENER),
                            45
                                    * 1000 /*45 seconds to change a checkbox is enough. After that, I wont listen to changes anymore.*/);
                    Intent startSettings = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                    startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startSettings.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    try {
                        context.startActivity(startSettings);
                    } catch (ActivityNotFoundException notFoundEx) {
                        // weird.. the device does not have the IME setting activity. Nook?
                        Toast.makeText(
                                        context,
                                        R.string
                                                .setup_wizard_step_one_action_error_no_settings_activity,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                };
        view.findViewById(R.id.go_to_language_settings_action)
                .setOnClickListener(goToDeviceLanguageSettings);
        mStateIcon.setOnClickListener(goToDeviceLanguageSettings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        mBaseContext = activity.getBaseContext();
        mReLaunchTaskIntent = new Intent(mBaseContext, SetupWizardActivity.class);
        mReLaunchTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGetBackHereHandler.removeMessages(KEY_MESSAGE_RETURN_TO_APP);
        unregisterSettingsObserverNow();
    }

    @Override
    public void refreshFragmentUi() {
        super.refreshFragmentUi();
        if (getActivity() != null) {
            final boolean isEnabled = isStepCompleted(getActivity());
            mStateIcon.setImageResource(
                    isEnabled ? R.drawable.ic_wizard_enabled_on : R.drawable.ic_wizard_enabled_off);
            mStateIcon.setClickable(!isEnabled);
        }
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return SetupSupport.isThisKeyboardEnabled(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSettingsObserverNow();
    }

    private void unregisterSettingsObserverNow() {
        mGetBackHereHandler.removeMessages(KEY_MESSAGE_UNREGISTER_LISTENER);
        mSecureSettingsChangedDisposable.dispose();
    }
}
