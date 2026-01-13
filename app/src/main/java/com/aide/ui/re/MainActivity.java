package com.aide.ui.re;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.aide.ui.re.base.BaseAdapter;
import com.aide.ui.re.databinding.MainBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import com.google.android.material.color.MaterialColors;
import android.util.TypedValue;

public class MainActivity extends AppCompatActivity {
    private MainBinding binding;

    // 记录当前显示的目录
    private File currentDir;
    // 根目录 (用于判断是否到了最顶层)
    private File rootDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置沉浸式布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 设置 Toolbar
        setSupportActionBar(binding.toolbar);
        Drawable icon = resizeDrawable(this, R.drawable.ic_launcher, 36); // 建议换成汉堡菜单图标
        binding.toolbar.setNavigationIcon(icon);
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START);
            } else {
                binding.drawer.openDrawer(GravityCompat.START);
            }
        });

        // 2. 初始化根目录
        rootDir = new File(Environment.getExternalStorageDirectory(), "AppProjects");

        // 3. 初始化 RecyclerView (只设置一次 LayoutManager)
        binding.fileBrowserRv.setLayoutManager(new LinearLayoutManager(this));

        // 4. 初次加载数据
        loadFiles(rootDir);
    }

    /**
     * 核心方法：加载指定目录下的文件
     */
    private void loadFiles(File dir) {
        if (dir == null || !dir.exists()) return;

        this.currentDir = dir;

        // 获取文件夹列表 (过滤掉文件，只显示文件夹)
        File[] projectFiles = dir.listFiles(pathname -> pathname.isDirectory());

        List<File> filesList = new ArrayList<>();
        if (projectFiles != null) {
            filesList = Arrays.stream(projectFiles)
				.sorted((f1, f2) -> {
				// 排序逻辑：文件夹优先，然后按名称
				if (f1.isDirectory() && !f2.isDirectory()) return -1;
				if (!f1.isDirectory() && f2.isDirectory()) return 1;
				return f1.getName().compareToIgnoreCase(f2.getName());
			})
			.collect(Collectors.toList());
        }

        // 设置 Adapter
        BaseAdapter<File> adapter = new BaseAdapter<File>(this, R.layout.item_file_browser, filesList) {
            @Override
            protected void convert(BaseAdapter.BaseViewHolder holder, File item, int position) {
                holder.setText(R.id.file_name, item.getName());
                // 你可以在这里根据 item.isDirectory() 设置不同的图标
            }
        };

        // 设置点击事件：点击进入下一级
        adapter.setOnItemClickListener((view, position, item) -> {
            // 点击文件夹，进入该文件夹
            loadFiles(item);
        });

        binding.fileBrowserRv.setAdapter(adapter);

        // 更新面包屑导航
        updateBreadcrumb(dir);
    }

    /**
     * 更新面包屑导航
     * 注意：前提是 XML 中必须存在 id 为 breadcrumb_container 的布局
     */
	private void updateBreadcrumb(File currentFile) {
        if (binding.breadcrumbContainer == null) return;

        android.widget.LinearLayout container = binding.breadcrumbContainer;
        container.removeAllViews();

        List<File> pathList = new ArrayList<>();
        File temp = currentFile;
        while (temp != null) {
            pathList.add(temp);
            if (temp.equals(rootDir.getParentFile())) break;
            temp = temp.getParentFile();
        }
        Collections.reverse(pathList);

        // 准备颜色
        int normalColor = MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnSurfaceVariant);
        int activeColor = MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnPrimaryContainer);

        // 【修正开始】：获取系统自带的水波纹效果
        TypedValue outValue = new TypedValue();
        // 解析系统通用的 selectableItemBackground 属性
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        // 转换为 Drawable 对象
        Drawable ripple = getResources().getDrawable(outValue.resourceId);
        // 【修正结束】

        for (int i = 0; i < pathList.size(); i++) {
            final File fileSegment = pathList.get(i);
            boolean isLastItem = (i == pathList.size() - 1);

            TextView segmentView = new TextView(this);
            String name = fileSegment.getName();
            if (name.isEmpty()) name = "Root";

            segmentView.setText(name);
            segmentView.setTextSize(14);
            segmentView.setGravity(Gravity.CENTER_VERTICAL);

            if (isLastItem) {
                // === 当前目录：高亮背景 ===
                segmentView.setBackgroundResource(R.drawable.bg_breadcrumb_active);
                segmentView.setTextColor(activeColor);
                segmentView.setTypeface(null, android.graphics.Typeface.BOLD);
                segmentView.setOnClickListener(null);
            } else {
                // === 父级目录：使用刚才获取的 ripple 效果 ===
                segmentView.setBackground(ripple); // 这里直接用上面解析出来的 ripple
                segmentView.setTextColor(normalColor);
                segmentView.setOnClickListener(v -> loadFiles(fileSegment));
            }

            container.addView(segmentView);

            if (i < pathList.size() - 1) {
                TextView arrowView = new TextView(this);
                arrowView.setText(" > ");
                arrowView.setTextSize(14);
                arrowView.setTextColor(MaterialColors.getColor(container, com.google.android.material.R.attr.colorOnSurface));
                arrowView.setGravity(Gravity.CENTER_VERTICAL);
                container.addView(arrowView);
            }
        }
    }
	
	

    @Override
    public void onBackPressed() {
        // 1. 如果侧栏打开，优先关闭侧栏
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // 2. 如果不在根目录，返回上一级目录
        if (currentDir != null && !currentDir.equals(rootDir)) {
            File parent = currentDir.getParentFile();
            if (parent != null && parent.exists()) {
                loadFiles(parent);
                return;
            }
        }

        // 3. 否则，默认退出
        super.onBackPressed();
    }

    // 工具方法：调整 Drawable 大小
    public static Drawable resizeDrawable(Context context, int drawableRes, int sizeDp) {
        Drawable original = ContextCompat.getDrawable(context, drawableRes);
        if (original == null) return null;

        float density = context.getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * density + 0.5f);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        original.setBounds(0, 0, sizePx, sizePx);
        original.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }
}

