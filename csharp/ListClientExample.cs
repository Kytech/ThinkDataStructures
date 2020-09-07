using System;
using System.Collections;
using System.Collections.Generic;

public class ListClientExample {
	private ICollection list;
	public ListClientExample() {
		list = new LinkedList<int>();
	}
	public ICollection getList() {
		return list;
	}
	public static void Main(string[] args) {
		ListClientExample lce = new ListClientExample();
		ICollection list = lce.getList();
		Console.WriteLine(list);
	}
}
