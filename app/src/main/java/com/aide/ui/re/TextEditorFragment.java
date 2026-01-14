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
		editText.setText(FileUtils.read(new File(currentFilePath)));
		

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
		// 注意：这里假设从文件加载也在 setText 发生，或者恢复后即为初始状态
		recordInitialState();

		editText.getText().addContentListener(new ContentListener() {

			@Override
			public void afterDelete(Content content, int p, int p1, int p2, int p3, CharSequence charSequence) {
				// TODO: Implement this method
				checkUnsavedStatus();

			}

			@Override
			public void afterInsert(Content content, int p, int p1, int p2, int p3, CharSequence charSequence) {
				// TODO: Implement this method
				checkUnsavedStatus();

			}

			@Override
			public void beforeReplace(Content content) {
				checkUnsavedStatus();

			}
		});

		return view;
	}

	// 记录初始状态
	private void recordInitialState() {
		// 确保 Content 已准备好
		if (editText.getText() != null) {
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

		String currentText = editText.getText().toString();
		boolean isDirty = !currentText.equals(initialText);

		// 只有状态改变时才通知（例如从干净变为脏，反之亦然）
		// 注意：这里简化逻辑，如果用户修改后改回原样，红点会消失。
		// 如果您希望“一旦修改就永久变脏直到保存”，需要去掉这个反向判断或增加保存逻辑。
		if (isDirty != isNotifiedUnsaved) {
			isNotifiedUnsaved = isDirty;
			((MainActivity) getActivity()).updateTabUnsavedStatus(currentFilePath, isDirty);
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("saved_path", currentFilePath);
		outState.putString("saved_text", editText.getText().toString());
	}
}

