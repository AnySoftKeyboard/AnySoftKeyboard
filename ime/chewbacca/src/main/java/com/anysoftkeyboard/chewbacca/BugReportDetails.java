package com.anysoftkeyboard.chewbacca;

import android.net.Uri;
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
    public final String crashHeader;
    public final Uri fullReport;

    public BugReportDetails(String crashHeader, String crashReportText, Uri fullReport) {
        this.crashHeader = crashHeader;
        this.crashReportText = crashReportText;
        this.fullReport = fullReport;
    }

    public BugReportDetails(Parcel in) {
        crashHeader = in.readString();
        crashReportText = in.readString();
        fullReport = in.readParcelable(BugReportDetails.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(crashHeader);
        dest.writeString(crashReportText);
        dest.writeParcelable(fullReport, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
