package com.anysoftkeyboard.utils;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PointFCompat implements Parcelable {

    private PointF mPoint;

    public PointFCompat(PointF point) {
        mPoint = point;
    }

    // Start of Parcel part
    public PointFCompat(Parcel in){
        float[] data = new float[2];

        in.readFloatArray(data);
        mPoint = new PointF(data[0], data[1]);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(new float[]{mPoint.x, mPoint.y});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PointFCompat createFromParcel(Parcel in) {
            return new PointFCompat(in);
        }

        public PointFCompat[] newArray(int size) {
            return new PointFCompat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
    //End of Parcel part

    public PointF getPoint() {return mPoint;}

    public static PointF getPointFromBundle(Bundle bundle, String pointFCompactKey) {
        if (bundle == null)
            return null;

        PointFCompat compat = bundle.getParcelable(pointFCompactKey);
        if (compat == null)
            return null;

        return compat.getPoint();
    }
}
