package com.aide.ui.re.menuaction;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import androidx.appcompat.app.AlertDialog;
import com.aide.ui.re.MainActivity;
import com.aide.ui.re.R;
import com.aide.ui.re.utils.DensityUtils;
import com.aide.ui.re.view.M3EditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.io.IOException;

public class CreateNewFileMenu implements MenuImpl
{

	@Override
	public boolean isEnabled()
	{
		// TODO: Implement this method
		return true;
	}

	

	@Override
	public boolean isVisible()
	{
		// TODO: Implement this method
		return true;
	}


	
	@Override
	public int getMenuId() {
		// TODO: Implement this method
		return R.id.menu_new_file;
	}

	@Override
	public String getMenuTitle() {
		// TODO: Implement this method
		return "新建";
	}

	@Override
	public Drawable getMenuIcon() {
		// TODO: Implement this method
		return null;
	}

	@Override
	public void run(Activity activity,Object...p) {
		// TODO: Implement this method
		MainActivity mainActivity=(MainActivity)activity;
		M3EditText et = new M3EditText(activity);
		int dp=DensityUtils.dp2px(activity,24);
		et.setPadding(dp,DensityUtils.dp2px(activity,16),dp,0);
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
		builder.setTitle("新建");
		builder.setView(et);
		builder.setPositiveButton("文件夹",null);
		builder.setNegativeButton("文件", null);
		builder.setNeutralButton("取消", null);
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
			if(TextUtils.isEmpty(et.getText())) {
				et.setError("文件夹名称不能为空");
				return;
			}
			File f = new File(mainActivity.getCurrentDir(), et.getText().toString().trim());
			if (!f.exists())
				f.mkdirs();
			mainActivity.loadFiles(f);
			dialog.dismiss();
		});
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v->{
			if(TextUtils.isEmpty(et.getText())) {
				et.setError("文件名称不能为空");
				return;
			}

			File f = new File(mainActivity.getCurrentDir(), et.getText().toString().trim());
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			try {
				f.createNewFile();
				mainActivity.loadFiles(f.getParentFile(),true);
			} catch (IOException e) {
			}
			dialog.dismiss();
		});
	}

}

