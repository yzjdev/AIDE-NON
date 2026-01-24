package com.aide.ui.re.view;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.aide.ui.re.R;
import com.google.android.material.textfield.TextInputLayout;

public class M3EditText extends FrameLayout{

	TextInputLayout layout;
	EditText et;

	public M3EditText(Context context){
		this(context,null);
	}

	public M3EditText(Context context,AttributeSet attrs){
		super(context,attrs);
		init(context);
	}

	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.layout_m3_edit_text,this,true);
		layout = findViewById(R.id.input);
		et=layout.getEditText();
		et.setSingleLine(true);
	}

	public void setHint(CharSequence hint){
		layout.setHint(hint);

	}


	public void setError(CharSequence error){
		layout.setError(error);

	}
	public CharSequence getText(){
		return et.getText();
	}

	public void setText(CharSequence text){
		et.setText(text);
	}

}
