package com.aide.ui.re;

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

public class TextEditorFragment extends Fragment {
	private static final String ARG_FILE_PATH = "arg_file_path";

	private CodeEditor editText;
	private String currentFilePath;
	private String initialText; // 用于对比的初始文本
	private boolean isNotifiedUnsaved = false; // 防止重复通知

	// 保存 listener 引用以便在 onDestroyView 中移除
	private ContentListener contentListener;

	public static TextEditorFragment newInstance(String filePath) {
		TextEditorFragment fragment = new TextEditorFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FILE_PATH, filePath);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_text_editor, container, false);
		editText = view.findViewById(R.id.edit_text);
		if (getArguments() != null) {
			currentFilePath = getArguments().getString(ARG_FILE_PATH);
		}

		// 安全读文件：如果路径为空则设置空文本
		if (currentFilePath != null) {
			editText.setText(FileUtils.read(new File(currentFilePath)));
		} else {
			editText.setText("");
		}

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
			} catch (Exception ignored) {
				// 防御性容错：若 remove 不支持或抛异常则忽略
			}
		}
		editText = null;
		contentListener = null;
		super.onDestroyView();
	}
}
