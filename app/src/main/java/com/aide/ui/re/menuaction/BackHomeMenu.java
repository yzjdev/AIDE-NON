package com.aide.ui.re.menuaction;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import com.aide.ui.re.R;
import com.aide.ui.re.MainActivity;

public class BackHomeMenu implements MenuImpl {

	@Override
	public int getMenuId() {
		// TODO: Implement this method
		return R.id.menu_back_home;
	}

	@Override
	public String getMenuTitle() {
		// TODO: Implement this method
		return "项目";
	}

	@Override
	public Drawable getMenuIcon() {
		// TODO: Implement this method
		return null;
	}

	@Override
	public void run(Activity activity,Object...p) {
		// TODO: Implement this method
		MainActivity mainActivity = (MainActivity) activity;
		mainActivity.loadFiles(mainActivity.getRootDir());
	}

	@Override
	public boolean isVisible() {
		// TODO: Implement this method
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO: Implement this method
		return true;
	}

}

