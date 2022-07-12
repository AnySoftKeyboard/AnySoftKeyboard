package com.anysoftkeyboard.chewbacca;

import android.os.Parcel;
import android.os.Parcelable;

public class BugReportDetails implements Parcelable {
    public static final String EXTRA_KEY_BugReportDetails = "EXTRA_KEY_BugReportDetails";
    public static final Creator<BugReportDetails> CREATOR =
            new Creator<>() {
                @Override
                public BugReportDetails createFromParcel(Parcel in) {
                    return new BugReportDetails(in);
                }

                @Override
                public BugReportDetails[] newArray(int size) {
                    return new BugReportDetails[size];
                }
            };
    public final String crashReportText;

    public BugReportDetails(String crashReportText) {
        this.crashReportText = crashReportText;
    }

    public BugReportDetails(Parcel in) {
        crashReportText = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(crashReportText);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
