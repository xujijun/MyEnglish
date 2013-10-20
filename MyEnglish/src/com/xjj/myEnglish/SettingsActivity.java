package com.xjj.myEnglish;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);//设置返回按钮
		
		  // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
	}

}
