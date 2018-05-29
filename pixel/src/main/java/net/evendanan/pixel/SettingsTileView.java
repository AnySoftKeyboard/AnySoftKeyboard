package net.evendanan.pixel;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A custom view from a
 */
public class SettingsTileView extends LinearLayout {
    private TextView mLabel;
    private ImageView mImage;
    private Drawable mSettingsTile;
    private CharSequence mSettingsLabel;

    public SettingsTileView(Context context) {
        super(context);
        init(null);
    }

    public SettingsTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SettingsTileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setupBasicLayoutConfiguration();

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.SettingsTileView);

        mSettingsTile = array.getDrawable(R.styleable.SettingsTileView_tileImage);
        mSettingsLabel = array.getText(R.styleable.SettingsTileView_tileLabel);

        array.recycle();

        inflate(getContext(), R.layout.settings_tile_view, this);
    }

    private void setupBasicLayoutConfiguration() {
        setBackgroundResource(R.drawable.transparent_click_feedback_background);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setOrientation(LinearLayout.VERTICAL);
            setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
        } else {
            setOrientation(LinearLayout.HORIZONTAL);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImage = findViewById(R.id.tile_image);
        mImage.setImageDrawable(mSettingsTile);
        mLabel = findViewById(R.id.tile_label);
        mLabel.setText(mSettingsLabel);
        setupBasicLayoutConfiguration();
    }

    public CharSequence getLabel() {
        return mLabel.getText();
    }

    public void setLabel(CharSequence label) {
        mLabel.setText(label);
    }

    public Drawable getImage() {
        return mImage.getDrawable();
    }

    public void setImage(@DrawableRes int imageId) {
        mImage.setImageResource(imageId);
    }
}
