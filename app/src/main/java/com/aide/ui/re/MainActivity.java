package com.aide.ui.re;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.aide.ui.re.base.BaseActivity;
import com.aide.ui.re.base.BaseAdapter;
import com.aide.ui.re.databinding.MainBinding;
import com.google.android.material.color.MaterialColors;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import android.view.View;

public class MainActivity extends BaseActivity {

    private MainBinding binding;
    private File currentDir;
    private File rootDir;
    private BaseAdapter<File> fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupMoreMenu();

        rootDir = new File(Environment.getExternalStorageDirectory(), "AppProjects");
        loadFiles(rootDir);
    }

    // ======================
    // UI 初始化拆分
    // ======================

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        Drawable icon = resizeDrawable(this, R.drawable.ic_launcher, 36);
        binding.toolbar.setNavigationIcon(icon);
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START);
            } else {
                binding.drawer.openDrawer(GravityCompat.START);
            }
        });
    }

    private void setupRecyclerView() {
        binding.fileBrowserRv.setLayoutManager(new LinearLayoutManager(this));

        fileAdapter = new BaseAdapter<File>(this, R.layout.item_file_browser, new ArrayList<>()) {
            @Override
            protected void convert(BaseAdapter.BaseViewHolder holder, File item, int position) {
                holder.setText(R.id.file_name, item.getName());

                // 保持简单，只设置图片，不做额外着色处理（SVG 由你自己在资源里定义或处理）
                holder.setImageResource(R.id.icon,
										item.isDirectory()
										? R.drawable.ic_baseline_folder_24
										: R.drawable.ic_baseline_insert_drive_file_24);

                // 显示详情
                String timeStr = formatDate(item.lastModified());
                String detailsText;

                if (item.isDirectory()) {
                    File[] children = item.listFiles();
                    int count = (children != null) ? children.length : 0;
                    detailsText = timeStr + " · " + count + " 项";
                } else {
                    detailsText = timeStr + " · " + formatFileSize(item.length());
                }
                holder.setText(R.id.file_details, detailsText);
            }
        };

        fileAdapter.setOnItemClickListener((view, position, item) -> {
            if (item.isDirectory()) {
                loadFiles(item);
            }
            // 文件点击逻辑暂留
        });

        binding.fileBrowserRv.setAdapter(fileAdapter);
    }

    private void setupMoreMenu() {
        binding.fileBrowserMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.file_browser_popup, popup.getMenu());

            // Menu 图标保持原样，不做 tint 处理

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_refresh) {
                    loadFiles(currentDir);
                    return true;
                } else if (id == R.id.menu_new_folder) {
                    // TODO
                    return true;
                } else if (id == R.id.sort_name) {
                    // TODO
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    // ======================
    // 业务逻辑
    // ======================

	/**
     * 加载文件列表 (优化版：防止卡顿)
     */
    private void loadFiles(File dir) {
        // 1. 基础检查
        if (dir == null || !dir.exists()) return;

        // 2. 检查是否重复加载
        if (currentDir != null && currentDir.equals(dir)) return;

        this.currentDir = dir;

        // 3. 【优化点】开启子线程处理耗时操作
        new Thread(() -> {
            // A. 读取文件列表 (I/O 操作，耗时长)
            File[] allFiles = dir.listFiles();
            if (allFiles == null) allFiles = new File[0];

            // B. 排序文件列表 (计算操作，数据量大时耗时长)
            List<File> filesList = Arrays.stream(allFiles).sorted((f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f1.getName());
            }).collect(Collectors.toList());

            // C. 处理完成后，切回主线程更新 UI
            runOnUiThread(() -> {
                // 更新列表
                fileAdapter.setNewData(filesList);
                // 更新面包屑
                updateBreadcrumb(dir);
            });

        }).start(); // 启动线程
    }
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (currentDir != null && !currentDir.equals(rootDir)) {
            File parent = currentDir.getParentFile();
            if (parent != null && parent.exists()) {
                loadFiles(parent);
                return;
            }
        }
        super.onBackPressed();
    }

    // ======================
    // 辅助 UI
    // ======================

    private void updateBreadcrumb(File currentFile) {
        if (binding.breadcrumbContainer == null) return;
        android.widget.LinearLayout container = binding.breadcrumbContainer;

        container.removeAllViews();
        container.setGravity(Gravity.CENTER_VERTICAL);

        // 构建路径链
        List<File> pathList = new ArrayList<>();
        File temp = currentFile;
        while (temp != null) {
            pathList.add(temp);
            if (rootDir != null && temp.equals(rootDir.getParentFile())) break;
            temp = temp.getParentFile();
        }
        Collections.reverse(pathList);

        // 准备颜色
        int normalColor = MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnSurface);
        int activeColor = MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnPrimary);
        int arrowColor = MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnSurfaceVariant);

        // 准备背景
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        Drawable ripple = getResources().getDrawable(outValue.resourceId);

        // 填充视图
        for (int i = 0; i < pathList.size(); i++) {
            final File fileSegment = pathList.get(i);
            boolean isLastItem = (i == pathList.size() - 1);

            TextView segmentView =(TextView)View.inflate(this,R.layout.item_breadcrumb,null);// new TextView(this);
            String name = fileSegment.getName();
            if (name.isEmpty()) name = "Root";

            segmentView.setText(name);
            //segmentView.setTextSize(16);
            //segmentView.setGravity(Gravity.CENTER);

            if (isLastItem) {
              //  segmentView.setBackgroundResource(R.drawable.bg_breadcrumb_active);
                segmentView.setTextColor(activeColor);
            } else {
                segmentView.setBackground(ripple);
                segmentView.setTextColor(normalColor);
                segmentView.setOnClickListener(v -> loadFiles(fileSegment));
            }

            container.addView(segmentView);

            if (i < pathList.size() - 1) {
                ImageView arrowView = new ImageView(this);
                arrowView.setImageResource(R.drawable.ic_baseline_chevron_right_24);
                arrowView.setColorFilter(arrowColor);
                container.addView(arrowView);
            }
        }

        // 滚动到右侧 (这里直接使用类名，因为同包下不需要import)
        container.post(() -> {
            ViewParent parent = container.getParent();
            if (parent instanceof NestedHorizontalScroll) {
                int maxScroll = container.getWidth() - ((NestedHorizontalScroll) parent).getWidth();
                if (maxScroll < 0) maxScroll = 0;
                ((NestedHorizontalScroll) parent).scrollTo(maxScroll, 0);
            }
        });
    }
}

