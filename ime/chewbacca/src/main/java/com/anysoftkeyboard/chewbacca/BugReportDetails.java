package com.anysoftkeyboard.chewbacca;

import android.os.Parcel;
import android.os.Parcelable;

public class BugReportDetails implements Parcelable {
    public static final String EXTRA_KEY_BugReportDetails = "EXTRA_KEY_BugReportDetails";

    public final Throwable throwable;
    public final String crashReportText;

    public BugReportDetails(Throwable throwable, String crashReportText) {
        this.throwable = throwable;
        this.crashReportText = crashReportText;
    }

    // Start of Parcel part
    public BugReportDetails(Parcel in) {
        throwable = (Throwable) in.readSerializable();
        crashReportText = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(throwable);
        dest.writeString(crashReportText);
    }

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

    @Override
    public int describeContents() {
        return 0;
    }
    // End of Parcel part
}
