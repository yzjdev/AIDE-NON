package com.aide.ui.re.base;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity{

	/**
     * 格式化时间
     */
    public String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    /**
     * 格式化文件大小 (B, KB, MB)
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        int k = (int) (size / 1024);
        if (k < 1024) {
            return k + " KB";
        }
        int m = k / 1024;
        return m + " MB";
    }
	

	// 工具方法：调整 Drawable 大小
	public static Drawable resizeDrawable(Context context, int drawableRes, int sizeDp) {
		Drawable original = ContextCompat.getDrawable(context, drawableRes);
		if (original == null)
			return null;

		float density = context.getResources().getDisplayMetrics().density;
		int sizePx = (int) (sizeDp * density + 0.5f);

		Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		original.setBounds(0, 0, sizePx, sizePx);
		original.draw(canvas);

		return new BitmapDrawable(context.getResources(), bitmap);
	}
}
