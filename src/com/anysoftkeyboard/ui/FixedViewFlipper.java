package com.anysoftkeyboard.ui;

import com.menny.android.anysoftkeyboard.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

/*
 * http://daniel-codes.blogspot.com/2010/05/viewflipper-receiver-not-registered.html
 */
public class FixedViewFlipper extends ViewFlipper {

	public FixedViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDetachedFromWindow() {
	    try {
	        super.onDetachedFromWindow();
	    }
	    catch (IllegalArgumentException e) {
	        stopFlipping();
	    }
	}
	
	@Override
	public void setDisplayedChild(int whichChild) {
		final int current = getDisplayedChild();
		if (current == whichChild) return;
		final Context c = getContext().getApplicationContext();
		if (whichChild > current)
		{
	        setInAnimation(c, R.anim.slide_in_right);
	        setOutAnimation(c, R.anim.slide_out_left);
		}
		else if (whichChild < current)
		{
			setInAnimation(c, R.anim.slide_in_left);
	        setOutAnimation(c, R.anim.slide_out_right);
		}
		super.setDisplayedChild(whichChild);
	}
}
