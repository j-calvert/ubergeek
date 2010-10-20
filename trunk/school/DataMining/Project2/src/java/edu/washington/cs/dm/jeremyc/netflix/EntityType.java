package edu.washington.cs.dm.jeremyc.netflix;

/**
 * We make use of the duality of movie and user, and where logic is usable in
 * both contexts, we use this enum to indicate which one we're using.
 */
public enum EntityType {
	USER, MOVIE;

	public EntityType dual() {
		if (this == USER) {
			return MOVIE;
		} else {
			return USER;
		}
	}

	public int select(int userId, int movieId) {
		if (this == USER) {
			return userId;
		} else {
			return movieId;
		}
	}

	public long getId(int userId, int movieId) {
		if (this == USER) {
			return concat(userId, movieId);
		} else {
			return concat(movieId, userId);
		}
	}

	public static long concat(int id1, int id2) {
		return ((long) id1) * ((long) Integer.MAX_VALUE) + (long) id2;
	}
}
