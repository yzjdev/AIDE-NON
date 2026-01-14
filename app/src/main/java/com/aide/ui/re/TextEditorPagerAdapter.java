package com.aide.ui.re;
// 在 TextEditorPagerAdapter.java 中

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.io.File;
import java.util.List;

public class TextEditorPagerAdapter extends FragmentStateAdapter {
    private List<File> dataList;

    public TextEditorPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity, List<File> dataList) {
        super(fragmentActivity);
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        File file = dataList.get(position);

        // 【关键修改】使用传入文件路径的方式创建 Fragment
        // 这样 Fragment 就知道自己属于哪个文件
        return TextEditorFragment.newInstance(file.getAbsolutePath());

        // 如果你之前是直接 new 的，请改成上面这样：
        // return new TextEditorFragment(); // ❌ 旧代码会导致无法获取路径
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // 如果需要更稳定的 ID (防止 ViewPager2 复用问题)，建议加上这个
    @Override
    public long getItemId(int position) {
        // 使用文件绝对路径的 HashCode 作为 ID，确保文件不同 ID 就不同
        return dataList.get(position).getAbsolutePath().hashCode();
    }
}

