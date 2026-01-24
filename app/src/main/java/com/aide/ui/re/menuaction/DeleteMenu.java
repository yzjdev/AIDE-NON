package com.aide.ui.re.menuaction;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import com.aide.ui.re.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import com.aide.ui.re.MainActivity;

public class DeleteMenu implements MenuImpl {

	@Override
	public int getMenuId() {
		// TODO: Implement this method
		return R.id.menu_file_delete;
	}

	@Override
	public String getMenuTitle() {
		// TODO: Implement this method
		return "删除";
	}

	@Override
	public Drawable getMenuIcon() {
		// TODO: Implement this method
		return null;
	}
	
	@Override
	public void run(Activity activity,Object... params) {
		File item=(File)params[0];
		MainActivity mainActivity=(MainActivity)activity;
		// TODO: Implement this method
		MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(activity);
		b.setTitle("删除");
		b.setMessage("是否删除文件 " + item.getName() + " ?\n删除后无法恢复");
		b.setPositiveButton("确定", (d, w) -> {
			item.delete();
			mainActivity.loadFiles(item.getParentFile(), true);
		});
		b.setNegativeButton("取消", null);
		b.show();
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

