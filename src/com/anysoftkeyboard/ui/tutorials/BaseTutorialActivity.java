package com.anysoftkeyboard.ui.tutorials;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.menny.android.anysoftkeyboard.R;

public abstract class BaseTutorialActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(getTitleResId());
		setContentView(getLayoutResId());
		

		View content = getWindow().getDecorView();
		//now to listen on all known buttons
		setClickHandler(content);
	}

	protected void setClickHandler(View content) {
		if (content instanceof ViewGroup)
		{
			for(View touchable : content.getTouchables())
			{
				setClickHandler(touchable);
			}
		}
		else
		{
			if ((content instanceof Button) || (content instanceof ImageButton))
			{
				content.setOnClickListener(this);
			}
		}
	}
	
	protected abstract int getLayoutResId();

	protected abstract int getTitleResId();

	public void onCloseClicked(View view)
	{
		finish();
	}
	
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.close_button:
			onCloseClicked(v);
			break;
		}
	}
}
