package com.aide.ui.re;
import com.google.android.material.textfield.TextInputLayout;
import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.EditText;
import com.aide.ui.re.utils.DensityUtils;

public class M3EditText extends FrameLayout{

	TextInputLayout layout;
	TextInputEditText et;
	public M3EditText(Context context){
		this(context,null);
	}
	
	public M3EditText(Context context,AttributeSet attrs){
		super(context,attrs);

		int dp=DensityUtils.dp2px(context,24);
		setPadding(dp,0,dp,0);
		
		layout=new TextInputLayout(context);
		et=new TextInputEditText(context);
		et.setSingleLine(true);
		layout.addView(et);
		
		addView(layout);
	}
	
	public EditText getEditText(){
		return layout.getEditText();
	}
	
	public CharSequence getText(){
		return getEditText().getText();
	}
	
	public void setText(CharSequence text){
		getEditText().setText(text);
	}
}
