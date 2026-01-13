package com.aide.ui.re.utils;

import android.content.Context;

/**
 * 屏幕密度单位转换工具类
 */
public class DensityUtils {

    /**
     * dp 转 px
     *
     * @param context 上下文
     * @param dpVal   dp 值
     * @return px 值
     */
    public static int dp2px(Context context, float dpVal) {
        // 获取屏幕密度
        final float scale = context.getResources().getDisplayMetrics().density;
        // +0.5f 是为了四舍五入，取整更精确
        return (int) (dpVal * scale + 0.5f);
    }

    /**
     * sp 转 px
     *
     * @param context 上下文
     * @param spVal   sp 值
     * @return px 值
     */
    public static int sp2px(Context context, float spVal) {
        // 获取缩放后的字体密度（支持用户字体大小设置）
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spVal * scale + 0.5f);
    }

    /**
     * px 转 dp (反向转换，附带赠送)
     */
    public static int px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxVal / scale + 0.5f);
    }

    /**
     * px 转 sp (反向转换，附带赠送)
     */
    public static int px2sp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxVal / scale + 0.5f);
    }
}
