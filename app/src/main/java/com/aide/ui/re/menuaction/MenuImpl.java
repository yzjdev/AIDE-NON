package com.aide.ui.re.menuaction;
import android.graphics.drawable.Drawable;
import com.aide.ui.re.MainActivity;
import android.app.Activity;

public interface MenuImpl{
	int getMenuId();
	String getMenuTitle();
	Drawable getMenuIcon();
	void run(Activity activity,Object...t);
	boolean isVisible();
	boolean isEnabled();
}

