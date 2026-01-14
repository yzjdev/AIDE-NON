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
import com.google.android.material.color.MaterialColors;
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

    private OnItemClickListener<T> mListener;
    private OnItemLongClickListener<T> mLongListener;

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

        // 设置点击监听器
        if (mListener != null) {
            holder.itemView.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(holder.itemView, pos, mData.get(pos));
                }
            });
        }

        // 设置长按监听器
        if (mLongListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mLongListener.onItemLongClick(holder.itemView, pos, mData.get(pos));
                }
                return true; // 消费掉长按事件，防止后面再触发点击
            });
        }
    }

    public List<T> getData() {
        return mData;
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

    // ============================
    // 点击事件接口定义
    // ============================
    public interface OnItemClickListener<T> {
        void onItemClick(View view, int position, T item);
    }

    public interface OnItemLongClickListener<T> {
        void onItemLongClick(View view, int position, T item);
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.mLongListener = listener;
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

        // --- 以下是常用的辅助方法，支持链式调用 ---

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

        public BaseViewHolder setBackgroundRes(int viewId, int resId) {
            getView(viewId).setBackgroundResource(resId);
            return this;
        }

		public BaseViewHolder setTextColor(int viewId, int color) {
            TextView tv=getView(viewId);
			tv.setTextColor(color);
            return this;
        }
		
        /**
         * 设置 ImageView 的颜色，支持传入主题属性
         * 例如：holder.setImageViewTint(R.id.icon, R.attr.colorPrimary);
         */
        public BaseViewHolder setImageViewTint(int viewId, int attrRes) {
            ImageView view = getView(viewId);
            if (view != null) {
                // 解析属性 (attr) 为具体的颜色值
                int color = MaterialColors.getColor(view, attrRes);
                // 设置颜色过滤器
                view.setColorFilter(color);
            }
            return this;
        }
    }
}

