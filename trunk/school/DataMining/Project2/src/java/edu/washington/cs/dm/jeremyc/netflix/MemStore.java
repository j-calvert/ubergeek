package edu.washington.cs.dm.jeremyc.netflix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The in-memory store of the training data. Consists of two maps, one from
 * MovieID to all Ratings for that movie and one from UserID to all ratings by
 * that user.
 */
public class MemStore {

	/**
	 * All ratings for each movie
	 */
	Map<Integer, Ratings> movies;

	/**
	 * All ratings by each user
	 */
	Map<Integer, Ratings> users;

	/**
	 * A simple wrapper around List&lt;Rating&gt; that caches the average of the
	 * rating values, and provides an init() method, called after the list is
	 * filled, to ensure the Rating list is sorted by ID and the average
	 * computed.
	 * 
	 */
	static class Ratings extends ArrayList<Rating> {
		private static final long serialVersionUID = 1L;

		float ave;

		public void init() {
			Collections.sort(this);
			ave = 0;
			for (Rating ur : this) {
				ave += ur.rating;
			}
			ave = ave / this.size();
		}
	}

	/**
	 * An ID rating pair. If this is in a Ratings list for the movies map, then
	 * the ID is a user ID, and vice versa.
	 */
	static class Rating implements Comparable<Rating> {
		/**
		 * The movieId or userId
		 */
		int id;

		/**
		 * The rating
		 */
		byte rating;

		Rating(int id, byte rating) {
			this(id);
			this.rating = rating;
		}

		Rating(int id) {
			this.id = id;
		}

		/**
		 * Used to sort the list of ratings for a given user or movie (we assume
		 * the sort for efficient one-pass calculation of the correlation of two
		 * movies or two users).
		 */
		public int compareTo(Rating o) {
			return id - o.id;
		}
	}

	/**
	 * Constructs a new empty MemStore
	 */
	public MemStore() {
		movies = new HashMap<Integer, Ratings>();
		users = new HashMap<Integer, Ratings>();
	}

	/**
	 * Gets a map based depending on the type
	 * 
	 * @param type
	 * @return the movies map if type == MOVIES, and the users map if type ==
	 *         USERS
	 */
	Map<Integer, Ratings> getMap(EntityType type) {
		return type == EntityType.USER ? users : movies;
	}

	/**
	 * Adds the rating to both maps.
	 * 
	 * @param movieId
	 * @param userId
	 * @param rating
	 */
	public void addRating(int movieId, int userId, byte rating) {
		for (EntityType type : EntityType.values()) {
			Map<Integer, Ratings> ratingsMap = getMap(type);
			Ratings ratings = ratingsMap.get(type.select(userId, movieId));
			if (ratings == null) {
				ratings = new Ratings();
			}
			ratings
					.add(new Rating(type.dual().select(userId, movieId), rating));
			ratingsMap.put(type.select(userId, movieId), ratings);
		}
	}

	/**
	 * Gets the ids of the movies rated by a user (if type == USER), or the
	 * users who have rated a movie (otherwise).
	 * 
	 * @param type
	 * @param id
	 */
	public List<Integer> getRelatedIds(EntityType type, int id) {
		List<Integer> ret = new ArrayList<Integer>();
		for (Rating r : getMap(type).get(id)) {
			ret.add(r.id);
		}
		return ret;
	}

	/**
	 * Gets the movie ratings made by a user (if type == USER), or the users'
	 * ratings who have rated a movie (otherwise).
	 * 
	 * @param type
	 * @param id
	 */
	public Ratings getRatings(EntityType type, int id) {
		return getMap(type).get(id);
	}

	/**
	 * Gets a single user's rating of a given movie (if for some reason it's not
	 * present, it returns 3...but this should never happen).
	 * 
	 * @param userId
	 * @param movieId
	 */
	public byte getRating(int userId, int movieId) {
		try {
			Ratings ratings = users.get(userId);
			int index = Collections.binarySearch(ratings, new Rating(movieId));
			return ratings.get(index).rating;
		} catch (RuntimeException e) {
			return 3;
		}
	}

}
