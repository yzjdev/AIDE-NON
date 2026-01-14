package com.aide.ui.re;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.aide.ui.re.base.BaseActivity;
import com.aide.ui.re.base.BaseAdapter;
import com.aide.ui.re.databinding.MainBinding;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.tencent.mmkv.MMKV;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.widget.TextView;
import android.view.LayoutInflater;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends BaseActivity {

    private MainBinding binding;
    private File currentDir;
    private File rootDir;
    private BaseAdapter<File> fileAdapter;
    private BaseAdapter<File> breadcrumbAdapter;

    // --- 成员变量 ---
    private List<File> openedFiles = new ArrayList<>(); // 存储已打开的文件
    private TextEditorPagerAdapter pagerAdapter;        // ViewPager2 适配器
    private int selectedFilePosition = -1;              // 文件列表当前选中的位置

    private MMKV mmkv;
    private Gson gson;

	// 【新增】用于记录文件未保存状态的 Map
    // Key: 文件绝对路径, Value: true(未保存) / false(已保存)
    private Map<String, Boolean> unsavedFileMap = new HashMap<>();
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化MMKV
        mmkv = MMKV.defaultMMKV();
        gson = new Gson();

        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupBreadcrumbRv();
        setupMoreMenu();

        setupPager();

        // Empty View 按钮点击事件
        binding.btnOpenDrawer.setOnClickListener(v -> {
            if (!binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.openDrawer(GravityCompat.START);
            }
        });

        rootDir = new File(Environment.getExternalStorageDirectory(), "AppProjects");
        loadFiles(rootDir);

        // 读取已保存的文件列表
        loadSavedFiles();

        updateUIState();
    }

    // --- setupPager (包含 Tab 再次单击弹出菜单逻辑) ---
    private void setupPager() {
        ViewPager2 viewPager = binding.viewPager;
        TabLayout tabLayout = binding.tabLayout;

		
        pagerAdapter = new TextEditorPagerAdapter(this, openedFiles);
		
		viewPager.setAdapter(pagerAdapter);
		viewPager.setUserInputEnabled(false);
        
		new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            File file = openedFiles.get(position);
            String filePath = file != null ? file.getAbsolutePath() : "";

            // 1. 获取或创建 Tab 的自定义视图
            View customView = tab.getCustomView();
            if (customView == null) {
                customView = LayoutInflater.from(this).inflate(R.layout.view_custom_tab, tabLayout, false);
                tab.setCustomView(customView);
            }

            // 2. 设置文件名
            TextView titleView = customView.findViewById(R.id.tab_title);
            titleView.setText(file != null ? file.getName() : "Unknown");

            // 3. 【关键】从 Map 中获取该文件的未保存状态，并更新 UI
            boolean isUnsaved = unsavedFileMap.containsKey(filePath) && unsavedFileMap.get(filePath);
            View dot = customView.findViewById(R.id.tab_dot);
            if (dot != null) {
                dot.setVisibility(isUnsaved ? View.VISIBLE : View.GONE);
            }
			}).attach();

        // 监听页面切换，同步文件列表高亮
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
				@Override
				public void onPageSelected(int position) {
					super.onPageSelected(position);
					if (position >= 0 && position < openedFiles.size()) {
						updateFileListHighlight(openedFiles.get(position));
					}
				}
			});

        // Tab 再次单击监听，弹出菜单
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
				@Override
				public void onTabSelected(TabLayout.Tab tab) { }

				@Override
				public void onTabUnselected(TabLayout.Tab tab) { }

				@Override
				public void onTabReselected(TabLayout.Tab tab) {
					showTabMenu(tab.getPosition(), tab.view);
				}
			});
    }
	
	/**
     * 【新增】公共方法：用于更新指定文件的 Tab 未保存状态
     * @param filePath 文件绝对路径
     * @param isUnsaved true 显示红点，false 隐藏红点
     */
	public void updateTabUnsavedStatus(String filePath, boolean isUnsaved) {
        // 更新内存中的状态记录
        unsavedFileMap.put(filePath, isUnsaved);

        // 更新 UI（如果 Tab 当前存在）
        runOnUiThread(() -> {
            if (openedFiles == null) return;

            for (int i = 0; i < openedFiles.size(); i++) {
                File file = openedFiles.get(i);
                if (file != null && file.getAbsolutePath().equals(filePath)) {
                    TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
                    if (tab != null && tab.getCustomView() != null) {
                        View dot = tab.getCustomView().findViewById(R.id.tab_dot);
                        if (dot != null) {
                            dot.setVisibility(isUnsaved ? View.VISIBLE : View.GONE);
                        }
                    }
                    break;
                }
            }
        });
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
        setTitle("AIDE");
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

    // --- setupRecyclerView (包含高亮和打开文件逻辑) ---
    private void setupRecyclerView() {
        binding.fileBrowserRv.setLayoutManager(new LinearLayoutManager(this));

        fileAdapter = new BaseAdapter<File>(this, R.layout.item_file_browser, new ArrayList<>()) {
            @Override
            protected void convert(BaseAdapter.BaseViewHolder holder, File item, int position) {
                holder.setText(R.id.file_name, item.getName());
                holder.setImageResource(R.id.icon,
										item.isDirectory()
										? R.drawable.ic_baseline_folder_24
										: R.drawable.ic_baseline_insert_drive_file_24);

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

                // 处理选中高亮背景
                if (position == selectedFilePosition && !item.isDirectory()) {
                    holder.itemView.setBackgroundColor(MaterialColors.getColor(holder.itemView,
																			   com.google.android.material.R.attr.colorSecondaryContainer));
                } else {
                    holder.itemView.setBackgroundColor(MaterialColors.getColor(holder.itemView,
																			   com.google.android.material.R.attr.colorSurface));
                }
            }
        };

        fileAdapter.setOnItemClickListener((view, position, item) -> {
            if (item.isDirectory()) {
                loadFiles(item); // 文件夹：进入目录
            } else {
                // 文件：打开文件
                if (!openedFiles.contains(item)) {
                    openedFiles.add(item);
                    pagerAdapter.notifyItemInserted(openedFiles.size() - 1);
                    saveOpenedFiles();
                    updateUIState();
                }

                // 更新当前选中位置
                int oldPosition = selectedFilePosition;
                selectedFilePosition = position;
                fileAdapter.notifyItemChanged(oldPosition);
                fileAdapter.notifyItemChanged(selectedFilePosition);

                // 切换 ViewPager
                int index = openedFiles.indexOf(item);
                binding.viewPager.setCurrentItem(index, true);

                // 关闭 Drawer
                binding.drawer.closeDrawer(GravityCompat.START);
            }
        });

        fileAdapter.setOnItemLongClickListener((view, position, item) -> {
            PopupMenu popup = new PopupMenu(this, view, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.file_browser_popup, popup.getMenu());
            popup.show();
        });
        binding.fileBrowserRv.setAdapter(fileAdapter);
    }

    private void saveOpenedFiles() {
        List<String> paths = openedFiles.stream()
			.map(File::getAbsolutePath)
		.collect(Collectors.toList());
        String json = gson.toJson(paths);
        mmkv.encode("opened_files_json", json);
    }

    private void loadSavedFiles() {
        String json = mmkv.decodeString("opened_files_json");
        if (json != null && !json.isEmpty()) {
            List<String> paths = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
            if (paths != null && !paths.isEmpty()) {
                for (String path : paths) {
                    File file = new File(path);
                    if (file.exists() && !openedFiles.contains(file)) {
                        openedFiles.add(file);
                    }
                }
                if (!openedFiles.isEmpty()) {
                    pagerAdapter.notifyDataSetChanged();
                    updateUIState();
                    binding.viewPager.setCurrentItem(0, true);
                }
            }
        }
    }

    private void setupBreadcrumbRv() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.breadcrumbRv.setLayoutManager(layoutManager);

        MyDividerItemDecoration decoration = new MyDividerItemDecoration(this, MyDividerItemDecoration.HORIZONTAL);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_chevron_right_24);
        if (dividerDrawable != null) {
            int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant,
												Color.GRAY);
            dividerDrawable.setTint(color);
            decoration.setDrawable(dividerDrawable);
        }
        binding.breadcrumbRv.addItemDecoration(decoration);

        binding.drawer.setRecyclerView(binding.breadcrumbRv);
        breadcrumbAdapter = new BaseAdapter<File>(this, R.layout.item_breadcrumb, new ArrayList<>()) {
            @Override
            protected void convert(BaseAdapter.BaseViewHolder holder, File item, int position) {
                String name = item.getName();
                if (name.isEmpty())
                    name = "Root";
                holder.setText(R.id.breadcrumb_text, name);

                boolean isLast = position == getData().size() - 1;
                if (isLast) {
                    holder.setBackgroundRes(R.id.breadcrumb_text, R.drawable.bg_breadcrumb_active);
                    holder.setTextColor(R.id.breadcrumb_text, MaterialColors.getColor(holder.itemView,
																					  com.google.android.material.R.attr.colorOnPrimaryContainer));
                } else {
                    holder.setBackgroundRes(R.id.breadcrumb_text, 0);
                    holder.setTextColor(R.id.breadcrumb_text, MaterialColors.getColor(holder.itemView,
																					  com.google.android.material.R.attr.colorOnSurface));
                }
            }
        };

        breadcrumbAdapter.setOnItemClickListener((view, position, file) -> loadFiles(file));
        binding.breadcrumbRv.setAdapter(breadcrumbAdapter);
    }

    private void setupMoreMenu() {
        binding.fileBrowserMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v, Gravity.END);
            popup.getMenuInflater().inflate(R.menu.breadcrumb_popup, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_refresh) {
                    loadFiles(currentDir);
                    return true;
                } else if (id == R.id.menu_new_file) {
                    M3EditText et = new M3EditText(this);
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                    builder.setTitle("新建");
                    builder.setView(et);
                    builder.setPositiveButton("文件夹", (d, w) -> {
                        File f = new File(currentDir, et.getText().toString().trim());
                        if (!f.exists())
                            f.mkdirs();
                        loadFiles(f);
                    });
                    builder.setNegativeButton("文件", (d, w) -> {
                        File f = new File(currentDir, et.getText().toString().trim());
                        if (!f.getParentFile().exists())
                            f.getParentFile().mkdirs();
                        try {
                            f.createNewFile();
                            loadFiles(f.getParentFile());
                        } catch (IOException e) {
                        }
                    });
                    builder.setNeutralButton("取消", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                } else if (id == R.id.menu_go_home) {
                    loadFiles(rootDir);
                    return true;
                } else if (id == R.id.menu_test) {
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    // --- 核心逻辑：loadFiles ---
    private void loadFiles(File dir) {
        if (dir == null || !dir.exists())
            return;
        if (currentDir != null && currentDir.equals(dir))
            return;

        this.currentDir = dir;
        selectedFilePosition = -1; // 切换目录清除选中

        new Thread(() -> {
            File[] allFiles = dir.listFiles();
            if (allFiles == null)
                allFiles = new File[0];

            List<File> filesList = Arrays.stream(allFiles).sorted((f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory())
                    return -1;
                if (!f1.isDirectory() && f2.isDirectory())
                    return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }).collect(Collectors.toList());

            runOnUiThread(() -> {
                fileAdapter.setNewData(filesList);
                updateBreadcrumb(dir);

                // 检查当前 Tab 文件是否在新列表中，更新高亮
                if (openedFiles != null && !openedFiles.isEmpty()) {
                    int currentTabPosition = binding.viewPager.getCurrentItem();
                    if (currentTabPosition >= 0 && currentTabPosition < openedFiles.size()) {
                        updateFileListHighlight(openedFiles.get(currentTabPosition));
                    }
                }
            });
        }).start();
    }

    // --- UI 状态切换方法 ---
    private void updateUIState() {
        if (openedFiles.isEmpty()) {
            // 无文件：显示 EmptyView，隐藏 Tab 和 Pager
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.tabLayout.setVisibility(View.GONE);
            binding.viewPager.setVisibility(View.GONE);
        } else {
            // 有文件：隐藏 EmptyView，显示 Tab 和 Pager
            binding.emptyView.setVisibility(View.GONE);
            binding.tabLayout.setVisibility(View.VISIBLE);
            binding.viewPager.setVisibility(View.VISIBLE);
        }
    }

    // --- Tab 菜单及关闭逻辑 ---
    private void showTabMenu(int position, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(Menu.NONE, 1, 1, "关闭当前");
        popup.getMenu().add(Menu.NONE, 2, 2, "关闭所有");
        popup.getMenu().add(Menu.NONE, 3, 3, "关闭其它");
        popup.getMenu().add(Menu.NONE, 4, 4, "关闭左边");
        popup.getMenu().add(Menu.NONE, 5, 5, "关闭右边");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) closeTab(position);
            else if (id == 2) closeAllTabs();
            else if (id == 3) closeOtherTabs(position);
            else if (id == 4) closeLeftTabs(position);
            else if (id == 5) closeRightTabs(position);
            return true;
        });
        popup.show();
    }

    private void closeTab(int position) {
        if (position < 0 || position >= openedFiles.size()) return;

        openedFiles.remove(position);
        pagerAdapter.notifyItemRemoved(position);
        saveOpenedFiles();
        if (openedFiles.isEmpty()) {
            updateUIState();
            selectedFilePosition = -1;
            fileAdapter.notifyDataSetChanged();
        } else {
            // 调整选中页
            int newPosition = (position >= openedFiles.size()) ? openedFiles.size() - 1 : position;
            binding.viewPager.setCurrentItem(newPosition, true);
        }
    }

    private void closeAllTabs() {
        openedFiles.clear();
        pagerAdapter.notifyDataSetChanged();
        updateUIState();
        mmkv.removeValueForKey("opened_files_json"); // 清除MMKV数据
        selectedFilePosition = -1;
        fileAdapter.notifyDataSetChanged();
    }

    // 关闭其它 Tab
    private void closeOtherTabs(int position) {
        if (position < 0 || position >= openedFiles.size()) return;
        File keepFile = openedFiles.get(position);
        openedFiles.clear();
        openedFiles.add(keepFile);
        pagerAdapter.notifyDataSetChanged();
        binding.viewPager.setCurrentItem(0, true);
        saveOpenedFiles();
    }

    // 关闭左边 Tab
    private void closeLeftTabs(int position) {
        if (position <= 0) return;
        openedFiles.subList(0, position).clear();
        pagerAdapter.notifyDataSetChanged();
        binding.viewPager.setCurrentItem(0, true);
        saveOpenedFiles();
    }

    // 关闭右边 Tab
    private void closeRightTabs(int position) {
        if (position >= openedFiles.size() - 1) return;
        openedFiles.subList(position + 1, openedFiles.size()).clear();
        pagerAdapter.notifyDataSetChanged();
        saveOpenedFiles();
    }

    // --- updateFileListHighlight ---
    private void updateFileListHighlight(File targetFile) {
        if (targetFile == null || fileAdapter.getData() == null || fileAdapter.getData().isEmpty()) {
            return;
        }

        int newPosition = -1;
        List<File> files = fileAdapter.getData();

        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getAbsolutePath().equals(targetFile.getAbsolutePath())) {
                newPosition = i;
                break;
            }
        }

        int oldPosition = selectedFilePosition;

        if (newPosition != -1) {
            selectedFilePosition = newPosition;
            if (oldPosition != -1) fileAdapter.notifyItemChanged(oldPosition);
            fileAdapter.notifyItemChanged(newPosition);

            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.fileBrowserRv.smoothScrollToPosition(newPosition);
            }
        } else {
            if (selectedFilePosition != -1) {
                selectedFilePosition = -1;
                fileAdapter.notifyItemChanged(oldPosition);
            }
        }
    }

    private void updateBreadcrumb(File currentFile) {
        if (currentFile == null || binding.breadcrumbRv == null)
            return;

        List<File> pathList = new ArrayList<>();
        File temp = currentFile;
        while (temp != null) {
            pathList.add(temp);
            if (rootDir != null && temp.equals(rootDir.getParentFile()))
                break;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_menu_exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentDir != null && !currentDir.equals(rootDir)) {
            File parent = currentDir.getParentFile();
            if (parent != null && parent.exists()) {
                loadFiles(parent);
                return;
            }
        }

        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
            return;
        }

        super.onBackPressed();
    }
}

