package com.aide.ui.re.menuaction;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import com.aide.ui.re.MainActivity;
import com.aide.ui.re.R;
import com.aide.ui.re.utils.DensityUtils;
import com.aide.ui.re.view.M3EditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;

public class RenameMenu implements MenuImpl {

	@Override
	public int getMenuId() {
		// TODO: Implement this method
		return R.id.menu_file_rename;
	}

	@Override
	public String getMenuTitle() {
		// TODO: Implement this method
		return "重命名";
	}

	@Override
	public Drawable getMenuIcon() {
		// TODO: Implement this method
		return null;
	}

	@Override
	public void run(Activity activity, Object... params) {
		File item=(File)params[0];
		
		// TODO: Implement this method
		MainActivity mainActivity=(MainActivity)activity;
		M3EditText et = new M3EditText(activity);
		et.setText(item.getName());
		int dp=DensityUtils.dp2px(activity,24);
		et.setPadding(dp,DensityUtils.dp2px(activity,16),dp,0);
		
		MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(mainActivity);
		builder.setTitle("重命名");
		builder.setView(et);
		builder.setPositiveButton("确定",null);
		builder.setNegativeButton("取消",null);
		builder.show();
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

