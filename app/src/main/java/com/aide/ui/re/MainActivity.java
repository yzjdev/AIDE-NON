package com.aide.ui.re;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aide.ui.re.base.BaseActivity;
import com.aide.ui.re.base.BaseAdapter;
import com.aide.ui.re.databinding.MainBinding;
import com.google.android.material.color.MaterialColors;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends BaseActivity {

    private MainBinding binding;
    private File currentDir;
    private File rootDir;
    private BaseAdapter<File> fileAdapter;
    private BaseAdapter<File> breadcrumbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupBreadcrumbRv();
        setupMoreMenu();

        rootDir = new File(Environment.getExternalStorageDirectory(), "AppProjects");
        loadFiles(rootDir);
    }

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
                holder.setImageResource(R.id.icon,
										item.isDirectory() ? R.drawable.ic_baseline_folder_24 : R.drawable.ic_baseline_insert_drive_file_24);

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
        });

        binding.fileBrowserRv.setAdapter(fileAdapter);
    }

    private void setupBreadcrumbRv() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.breadcrumbRv.setLayoutManager(layoutManager);

        // 启用嵌套滚动（配合自定义 DrawerLayout 使用）
        ViewCompat.setNestedScrollingEnabled(binding.breadcrumbRv, true);
        binding.breadcrumbRv.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // 设置分隔线（箭头）装饰器
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_chevron_right_24);
        if (dividerDrawable != null) {
            int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY);
            dividerDrawable.setTint(color);
            decoration.setDrawable(dividerDrawable);
        }
        binding.breadcrumbRv.addItemDecoration(decoration);

        // 【关键修改】关联 RecyclerView 到自定义 DrawerLayout，解决滑动冲突
        // 注意：前提是布局文件中已将 DrawerLayout 替换为 CustomDrawerLayout
        binding.drawer.setRecyclerView(binding.breadcrumbRv);

        breadcrumbAdapter = new BaseAdapter<File>(this, R.layout.item_breadcrumb, new ArrayList<>()) {
            @Override
            protected void convert(BaseAdapter.BaseViewHolder holder, File item, int position) {
                String name = item.getName();
                if (name.isEmpty()) name = "Root";
                holder.setText(R.id.breadcrumb_text, name);

                boolean isLast = (position == getData().size() - 1);
                int activeColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
                int normalColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorOnSurface);

                if (isLast) {
                    holder.setTextColor(R.id.breadcrumb_text, activeColor);
                    holder.setBackgroundRes(R.id.breadcrumb_text, R.drawable.bg_breadcrumb_active);
                } else {
                    holder.setTextColor(R.id.breadcrumb_text, normalColor);
                    holder.setBackgroundRes(R.id.breadcrumb_text, 0);
                }
            }
        };

        breadcrumbAdapter.setOnItemClickListener((view, position, file) -> loadFiles(file));
        binding.breadcrumbRv.setAdapter(breadcrumbAdapter);
    }

    private void setupMoreMenu() {
        binding.fileBrowserMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.file_browser_popup, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_refresh) {
                    loadFiles(currentDir);
                    return true;
                } else if (id == R.id.menu_new_folder) {
                    // TODO: 实现新建文件夹
                    return true;
                } else if (id == R.id.sort_name) {
                    // TODO: 实现排序
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void loadFiles(File dir) {
        if (dir == null || !dir.exists()) return;
        if (currentDir != null && currentDir.equals(dir)) return;

        this.currentDir = dir;

        new Thread(() -> {
            File[] allFiles = dir.listFiles();
            if (allFiles == null) allFiles = new File[0];

            List<File> filesList = Arrays.stream(allFiles).sorted((f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }).collect(Collectors.toList());

            runOnUiThread(() -> {
                fileAdapter.setNewData(filesList);
                updateBreadcrumb(dir);
            });
        }).start();
    }

    private void updateBreadcrumb(File currentFile) {
        if (currentFile == null || binding.breadcrumbRv == null) return;

        List<File> pathList = new ArrayList<>();
        File temp = currentFile;
        while (temp != null) {
            pathList.add(temp);
            if (rootDir != null && temp.equals(rootDir.getParentFile())) break;
            temp = temp.getParentFile();
        }
        Collections.reverse(pathList);

        breadcrumbAdapter.setNewData(pathList);

        binding.breadcrumbRv.post(() -> binding.breadcrumbRv.smoothScrollToPosition(pathList.size() - 1));
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

}

