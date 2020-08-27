package com.allendowney.thinkdast;

import java.util.List;
import java.util.ArrayList;

public class ListClientExample {
	@SuppressWarnings("rawtypes")
	private ArrayList list;

	@SuppressWarnings("rawtypes")
	public ListClientExample() {
		// This can't be new List() because an interface is abstract
		list = new ArrayList();
	}

	@SuppressWarnings("rawtypes")
	public List getList() {
		return list;
	}

	public static void main(String[] args) {
		ListClientExample lce = new ListClientExample();
		@SuppressWarnings("rawtypes")
		List list = lce.getList();
		System.out.println(list);
	}
}
