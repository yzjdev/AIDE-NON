package com.aide.ui.re.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView 通用适配器封装
 * @param <T> 数据类型
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.BaseViewHolder> {

    protected Context mContext;
    protected List<T> mData;
    protected int mLayoutId;

    // 修改处：这里也要带上泛型 <T>
    private OnItemClickListener<T> mListener;

    /**
     * 构造函数
     * @param context 上下文
     * @param layoutId Item 的布局 ID (例如 R.layout.item_user)
     * @param data 数据列表 (传 null 内部会自动初始化)
     */
    public BaseAdapter(Context context, int layoutId, List<T> data) {
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.mData = data == null ? new ArrayList<>() : data;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new BaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        // 获取当前位置的数据
        T item = mData.get(position);

        // 防止 RecyclerView 在删除/刷新动画时的越界问题
        if (item == null) return;

        // 调用抽象方法，让子类实现具体的绑定逻辑
        convert(holder, item, position);

        // 设置点击事件（如果有）
        if (mListener != null) {
            holder.itemView.setOnClickListener(v -> {
                // 获取最新的位置，防止 position 错乱
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(holder.itemView, pos, mData.get(pos));
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    //mListener.onItemLongClick(holder.itemView, pos, mData.get(pos));
                }
                return true;
            });
        }
    }

    /**
     * 子类必须实现此方法来填充数据
     * @param holder ViewHolder
     * @param item 当前数据
     * @param position 当前位置
     */
    protected abstract void convert(BaseViewHolder holder, T item, int position);

    /**
     * 设置新的数据（会刷新界面）
     */
    public void setNewData(List<T> list) {
        this.mData = list == null ? new ArrayList<>() : list;
        notifyDataSetChanged();
    }

    /**
     * 添加单条数据
     */
    public void addData(T data) {
        if (data != null) {
            mData.add(data);
            notifyItemInserted(mData.size() - 1);
        }
    }

    // --- 点击事件接口定义 ---
    // 修改重点：在 interface 后面加上 <T>，声明这是一个泛型接口
    public interface OnItemClickListener<T> {
        void onItemClick(View view, int position, T item);
       // void onItemLongClick(View view, int position, T item);
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.mListener = listener;
    }

    // ============================
    // ViewHolder 封装类
    // ============================
    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        // 使用 SparseArray 优化 View 查找性能
        private SparseArray<View> mViews;

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            mViews = new SparseArray<>();
        }

        /**
         * 根据 ID 获取 View (支持泛型，免去强转)
         */
        public <V extends View> V getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (V) view;
        }

        // --- 以下是常用的辅助方法，可以按需扩展 ---

        public BaseViewHolder setText(int viewId, CharSequence text) {
            TextView tv = getView(viewId);
            tv.setText(text);
            return this;
        }

        public BaseViewHolder setText(int viewId, int resId) {
            TextView tv = getView(viewId);
            tv.setText(resId);
            return this;
        }

        public BaseViewHolder setImageResource(int viewId, int resId) {
            ImageView iv = getView(viewId);
            iv.setImageResource(resId);
            return this;
        }

        public BaseViewHolder setVisibility(int viewId, int visibility) {
            getView(viewId).setVisibility(visibility);
            return this;
        }

        public BaseViewHolder setBackgroundColor(int viewId, int color) {
            getView(viewId).setBackgroundColor(color);
            return this;
        }

        // 如果需要更多方法，比如 setTag, setEnabled 等，都可以在这里链式扩展
    }
}

