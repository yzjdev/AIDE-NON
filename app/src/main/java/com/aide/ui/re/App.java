package com.aide.ui.re;
import io.github.zeroaicy.util.crash.CrashApplication;
import com.tencent.mmkv.MMKV;
import com.google.android.material.color.DynamicColors;

public class App extends CrashApplication
{

	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();

		DynamicColors.applyToActivitiesIfAvailable(this);
		MMKV.initialize(this);
	}

	
}
