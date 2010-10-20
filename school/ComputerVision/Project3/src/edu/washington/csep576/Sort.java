package edu.washington.csep576;


public enum Sort {

	COLOR, TEXTURE, COLOR_AND_TEXTURE;

	public static Sort fromString(String s) {
		if (s == null) {
			return COLOR;
		}
		for (Sort sort : values()) {
			if (s.equalsIgnoreCase(sort.toString())) {
				return sort;
			}
		}
		return COLOR;
	}

}
