package com.anysoftkeyboard.utils;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PointFCompact implements Parcelable {

    private PointF mPoint;

    public PointFCompact(PointF point) {
        mPoint = point;
    }

    // Parcelling part
    public PointFCompact(Parcel in){
        float[] data = new float[2];

        in.readFloatArray(data);
        mPoint = new PointF(data[0], data[1]);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(new float[]{mPoint.x, mPoint.y});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PointFCompact createFromParcel(Parcel in) {
            return new PointFCompact(in);
        }

        public PointFCompact[] newArray(int size) {
            return new PointFCompact[size];
        }
    };

    public PointF getPoint() {return mPoint;}

    @Override
    public int describeContents() {
        return 0;
    }

    public static PointF getPointFromBundle(Bundle bundle, String pointFCompactKey) {
        if (bundle == null)
            return null;

        PointFCompact compat = bundle.getParcelable(pointFCompactKey);
        if (compat == null)
            return null;

        return compat.getPoint();
    }
}
