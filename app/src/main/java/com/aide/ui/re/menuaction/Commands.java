package com.aide.ui.re.menuaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Commands{

	public static Map<Integer,MenuImpl> fileBrowserMenuMap=new HashMap<>();
	
	
	static{
		MenuImpl a=new CreateNewFileMenu();
		fileBrowserMenuMap.put(a.getMenuId(),a);
		a=new BackHomeMenu();
		fileBrowserMenuMap.put(a.getMenuId(),a);
		a=new DeleteMenu();
		fileBrowserMenuMap.put(a.getMenuId(),a);
		
	}
}
