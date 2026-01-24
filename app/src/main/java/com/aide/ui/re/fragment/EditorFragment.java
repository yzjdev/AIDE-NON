package com.aide.ui.re.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.aide.ui.re.R;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentListener;
import com.aide.ui.re.utils.FileUtils;
import java.io.File;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import android.content.res.Configuration;
import android.content.Context;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;
import io.github.rosemoe.sora.langs.java.JavaLanguage;
import android.graphics.Typeface;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import org.eclipse.tm4e.core.registry.IThemeSource;
import android.view.MotionEvent;
import com.aide.ui.re.MainActivity;
import io.github.rosemoe.sora.widget.SymbolInputView;
import java.util.Set;

public class EditorFragment extends Fragment {
	private static final String ARG_FILE_PATH = "arg_file_path";

	private CodeEditor editText;
	private String currentFilePath;
	private String initialText; // 用于对比的初始文本
	private boolean isNotifiedUnsaved = false; // 防止重复通知

	SymbolInputView symbolInput;
	// 保存 listener 引用以便在 onDestroyView 中移除
	private ContentListener contentListener;

	public static EditorFragment newInstance(String filePath) {
		EditorFragment fragment = new EditorFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FILE_PATH, filePath);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_text_editor, container, false);
		editText = view.findViewById(R.id.edit_text);
		symbolInput = view.findViewById(R.id.symbol_input);
		if (getArguments() != null) {
			currentFilePath = getArguments().getString(ARG_FILE_PATH);
		}

		if (FileUtils.isTextFile(new File(currentFilePath))) {

			editText.setText(FileUtils.read(new File(currentFilePath)));
		}else{
			editText.setText("不支持的类型");
		}
		
		/*
		// 1. 最优创建方式：Java 9+ Set.of()
		// 优点：代码极其简洁，不可变（线程安全，防止误修改）
		Set<String> textTypes = Set.of("txt", "java", "kt", "json", "xml", "cpp", "h","md","gradle");

		// 2. 使用逻辑
		int dotIndex = currentFilePath.lastIndexOf('.');
		if (currentFilePath != null 
			&& dotIndex > 0 
			&& textTypes.contains(currentFilePath.substring(dotIndex + 1).toLowerCase())) {

			editText.setText(FileUtils.read(new File(currentFilePath)));
		}else{
			editText.setText("不支持的类型");
		}
		*/

		// 1. 处理状态恢复
		if (savedInstanceState != null) {
			String savedPath = savedInstanceState.getString("saved_path");
			String savedText = savedInstanceState.getString("saved_text");

			// 路径匹配才恢复
			if (currentFilePath != null && currentFilePath.equals(savedPath)) {
				editText.setText(savedText);
			}
		}

		// 2. 记录当前文本为“初始文本”（作为判断未保存的基准）
		recordInitialState();

		// 保存 listener 引用，方便在 onDestroyView 中移除
		contentListener = new ContentListener() {

			@Override
			public void afterDelete(Content content, int p, int p1, int p2, int p3, CharSequence charSequence) {
				checkUnsavedStatus();
			}

			@Override
			public void afterInsert(Content content, int p, int p1, int p2, int p3, CharSequence charSequence) {
				checkUnsavedStatus();
			}

			@Override
			public void beforeReplace(Content content) {
				checkUnsavedStatus();
			}
		};

		// 有时 editText.getText() 可能为 null，先检查
		if (editText.getText() != null) {
			editText.getText().addContentListener(contentListener);
		}

		return view;
	}

	float lastX = 0f;
	float lastY = 0f;

	String[] SYMBOLS = {"TAB", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/", "<", ">", "[", "]",
			":"};

	String[] SYMBOL_INSERT_TEXT = {"\t", "{}", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/", "<", ">",
			"[", "]", ":"};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onViewCreated(view, savedInstanceState);
		editText.setTypefaceText(Typeface.MONOSPACE);
		symbolInput.bindEditor(editText);
		symbolInput.addSymbols(SYMBOLS, SYMBOL_INSERT_TEXT);
		editText.setOnTouchListener((v, event) -> {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN :
					lastX = event.getX();
					lastY = event.getY();
					// 默认编辑器先拦截
					if (editText.getParent() != null) {
						editText.getParent().requestDisallowInterceptTouchEvent(true);
					}
					break;

				case MotionEvent.ACTION_POINTER_DOWN :
					// 多点触控开始（缩放），完全拦截
					if (editText.getParent() != null) {
						editText.getParent().requestDisallowInterceptTouchEvent(true);
					}
					break;

				case MotionEvent.ACTION_MOVE :
					if (event.getPointerCount() > 1) {
						// 缩放中，完全拦截父布局
						if (editText.getParent() != null) {
							editText.getParent().requestDisallowInterceptTouchEvent(true);
						}
					} else {
						// 单指滑动
						float dx = event.getX() - lastX;
						float dy = event.getY() - lastY;

						if (Math.abs(dx) > Math.abs(dy)) {
							// 横向滑动，判断 scrollX 和 scrollMaxX
							if ((editText.getScrollX() >= editText.getScrollMaxX() && dx < 0)
									|| (editText.getScrollX() <= 0 && dx > 0)) {
								// 到边界，允许 Pager 消费
								if (editText.getParent() != null) {
									editText.getParent().requestDisallowInterceptTouchEvent(false);
								}
							} else {
								// 中间区域，编辑器处理
								if (editText.getParent() != null) {
									editText.getParent().requestDisallowInterceptTouchEvent(true);
								}
							}
						} else {
							// 纵向滑动，编辑器处理
							if (editText.getParent() != null) {
								editText.getParent().requestDisallowInterceptTouchEvent(true);
							}
						}

						lastX = event.getX();
						lastY = event.getY();
					}
					break;

				case MotionEvent.ACTION_UP :
				case MotionEvent.ACTION_CANCEL :
					// 手指抬起，恢复父布局默认拦截行为
					if (editText.getParent() != null) {
						editText.getParent().requestDisallowInterceptTouchEvent(false);
					}
					break;
			}

			// 让编辑器继续处理事件
			return false;
		});

		setupTextmate();

		ensureTextmateTheme();

		if (currentFilePath.endsWith(".java")) {
			editText.setEditorLanguage(new JavaLanguage());
		}

		ThemeRegistry.getInstance().setTheme("darcula");

		switchThemeIfRequired(requireActivity(), editText);
	}

	void setupTextmate() {
		FileProviderRegistry.getInstance()
				.addFileProvider(new AssetsFileResolver(requireActivity().getApplicationContext().getAssets()));

		var themes = new String[]{"darcula", "ayu-dark", "quietlight", "solarized_dark"};
		var themeRegistry = ThemeRegistry.getInstance();
		for (int i = 0; i < themes.length; i++) {
			var name = themes[i];
			var path = "textmate/" + name + ".json";
			var themeModel = new ThemeModel(IThemeSource
					.fromInputStream(FileProviderRegistry.getInstance().tryGetInputStream(path), path, null), name);
			if (name != "quietlight") {
				themeModel.setDark(true);
			}
		}

		themeRegistry.setTheme("darcula");

		GrammarRegistry.getInstance().loadGrammars("textmate/languages.json");

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO: Implement this method
		super.onConfigurationChanged(newConfig);
		if (editText != null)
			switchThemeIfRequired(requireActivity(), editText);
	}

	// 记录初始状态
	private void recordInitialState() {
		// 确保 Content 已准备好
		if (editText != null && editText.getText() != null) {
			initialText = editText.getText().toString();
		} else {
			initialText = "";
		}
		isNotifiedUnsaved = false;
	}

	// 检查是否未保存并通知 Activity
	private void checkUnsavedStatus() {
		if (getActivity() == null || !(getActivity() instanceof MainActivity))
			return;

		String currentText = "";
		if (editText != null && editText.getText() != null) {
			currentText = editText.getText().toString();
		}
		boolean isDirty = !currentText.equals(initialText);

		if (isDirty != isNotifiedUnsaved) {
			isNotifiedUnsaved = isDirty;
			((MainActivity) getActivity()).updateTabUnsavedStatus(currentFilePath, isDirty);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("saved_path", currentFilePath);
		if (editText != null && editText.getText() != null) {
			outState.putString("saved_text", editText.getText().toString());
		} else {
			outState.putString("saved_text", "");
		}
	}

	@Override
	public void onDestroyView() {
		// 移除 listener、清理引用，避免在 Activity/Fragment 重建时访问已销毁的 view 导致 crash
		if (editText != null && editText.getText() != null && contentListener != null) {
			try {
				editText.getText().removeContentListener(contentListener);
				editText.release();
			} catch (Exception ignored) {
				// 防御性容错：若 remove 不支持或抛异常则忽略
			}
		}
		editText = null;
		contentListener = null;
		super.onDestroyView();
	}

	public void ensureTextmateTheme() {
		var colorScheme = editText.getColorScheme();
		if (!(colorScheme instanceof TextMateColorScheme)) {
			colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance());
			editText.setColorScheme(colorScheme);
		}
	}

	public static void switchThemeIfRequired(Context context, CodeEditor editor) {
		if ((context.getResources().getConfiguration().uiMode
				& Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
			if (editor.getColorScheme() instanceof TextMateColorScheme) {
				ThemeRegistry.getInstance().setTheme("darcula");
			} else {
				editor.setColorScheme(new SchemeDarcula());
			}
		} else {
			if (editor.getColorScheme() instanceof TextMateColorScheme) {
				ThemeRegistry.getInstance().setTheme("quietlight");
			} else {
				editor.setColorScheme(new EditorColorScheme());
			}
		}
		editor.invalidate();
	}

}

