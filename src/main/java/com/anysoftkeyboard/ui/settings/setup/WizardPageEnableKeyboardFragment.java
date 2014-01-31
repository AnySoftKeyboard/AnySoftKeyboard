package com.anysoftkeyboard.ui.settings.setup;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

public class WizardPageEnableKeyboardFragment extends WizardPageBaseFragment {


    private static final int KEY_MESSAGE_UNREGISTER_LISTENER = 447;
    private static final int KEY_MESSAGE_RETURN_TO_APP = 446;


    private Handler mGetBackHereHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
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
            }
        }
    };

    private final ContentObserver mSecureSettingsChanged = new ContentObserver(null) {
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (!isResumed()) {
                if (isStepCompleted()) {
                    //should we return to this task?
                    //this happens when the user is asked to enable AnySoftKeyboard, which is done on a different UI activity (outside of my App).
                    mGetBackHereHandler.removeMessages(KEY_MESSAGE_RETURN_TO_APP);
                    mGetBackHereHandler.sendMessageDelayed(mGetBackHereHandler.obtainMessage(KEY_MESSAGE_RETURN_TO_APP), 50/*enough for the user to see what happened.*/);
                }
            }
        }
    };

    private Context mBaseContext = null;
    private Intent mReLaunchTaskIntent = null;
    private Context mAppContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.keyboard_setup_wizard_page_enable_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_language_settings_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //registering for changes, so I'll know to come back here.
                mAppContext = getActivity().getApplicationContext();
                mAppContext.getContentResolver().registerContentObserver(Settings.Secure.CONTENT_URI, true, mSecureSettingsChanged);
                //but I don't want to listen for changes for ever!
                //If the user is taking too long to change one checkbox, I say forget about it.
                mGetBackHereHandler.removeMessages(KEY_MESSAGE_UNREGISTER_LISTENER);
                mGetBackHereHandler.sendMessageDelayed(mGetBackHereHandler.obtainMessage(KEY_MESSAGE_UNREGISTER_LISTENER),
                        45*1000/*45 seconds to change a checkbox is enough. After that, I wont listen to changes anymore.*/);
                Intent startSettings = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    mAppContext.startActivity(startSettings);
                } catch (ActivityNotFoundException notFoundEx) {
                    //weird.. the device does not have the IME setting activity. Nook?
                    Toast.makeText(mAppContext, R.string.setup_wizard_step_one_action_error_no_settings_activity, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = getActivity();
        mBaseContext = activity.getBaseContext();
        mReLaunchTaskIntent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
    }

    @Override
    public void onStart() {
        super.onStart();
        mGetBackHereHandler.removeMessages(KEY_MESSAGE_RETURN_TO_APP);
        unregisterSettingsObserverNow();
    }

    @Override
    protected boolean isStepCompleted() {
        return SetupSupport.isThisKeyboardEnabled(getActivity());
    }

    @Override
    protected boolean isStepPreConditionDone() {
        return true;//the pre-condition is that the App is installed... I guess it does, right?
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSettingsObserverNow();
    }

    private void unregisterSettingsObserverNow() {
        mGetBackHereHandler.removeMessages(KEY_MESSAGE_UNREGISTER_LISTENER);
        if (mAppContext != null) {
            mAppContext.getContentResolver().unregisterContentObserver(mSecureSettingsChanged);
            mAppContext = null;
        }
    }
}
