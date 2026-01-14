package com.aide.ui.re;
// 在 TextEditorPagerAdapter.java 中

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.io.File;
import java.util.List;

public class TextEditorPagerAdapter extends FragmentStateAdapter {
    private List<File> dataList;

    // 保留原有的 FragmentActivity 构造器（Activity 中使用时可用）
    public TextEditorPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<File> dataList) {
        super(fragmentActivity);
        this.dataList = dataList;
    }

    // 新增：优先推荐在 Fragment 中传入 Fragment（会使用 childFragmentManager）
    public TextEditorPagerAdapter(@NonNull Fragment fragment, List<File> dataList) {
        super(fragment);
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public androidx.fragment.app.Fragment createFragment(int position) {
        File file = dataList.get(position);
        return TextEditorFragment.newInstance(file.getAbsolutePath());
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    // 当数据发生变化时请调用 setDataList(...) 并 notifyDataSetChanged()
    public void setDataList(List<File> newList) {
        this.dataList = newList;
        notifyDataSetChanged();
    }

    // 提供稳定 ID，帮助 FragmentStateAdapter 恢复时匹配 item
    @Override
    public long getItemId(int position) {
        if (dataList == null || position < 0 || position >= dataList.size()) {
            return RecyclerView.NO_ID;
        }
        String path = dataList.get(position).getAbsolutePath();
        // 使用路径的 hashCode 转为无符号 long，减少负值/符号问题
        return ((long) path.hashCode()) & 0xffffffffL;
    }

    // 与 getItemId 对应，判断当前 adapter 是否包含该 itemId
    @Override
    public boolean containsItem(long itemId) {
        if (dataList == null) return false;
        for (File f : dataList) {
            String path = f.getAbsolutePath();
            long id = ((long) path.hashCode()) & 0xffffffffL;
            if (id == itemId) return true;
        }
        return false;
    }
}
