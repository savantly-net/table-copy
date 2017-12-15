package net.savantly.data.table.copy;

import java.util.ArrayList;

public class DynamicRecord extends ArrayList<Object>{

	public DynamicRecord(Object... objects) {
		super();
		for (int i = 0; i < objects.length; i++) {
			this.add(objects[i]);
		}
	}
}
