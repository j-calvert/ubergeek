package edu.washington.cs.dm.jeremyc.netflix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.dm.jeremyc.netflix.MemStore.Ratings;

/**
 * Reads the contents of a text file in the format provided for the project
 * (e.g. "TestingRatings.txt"), creates a MemStore, populates it with this data,
 * and returns it.
 */
public class Populator {

	/**
	 * A simple container for a movieId, userId, rating triple.
	 * 
	 * @author jeremyc
	 * 
	 */
	static class DataRow {
		short movieId;

		int userId;

		byte rating;

		DataRow(short movieId, int userId, byte rating) {
			this.movieId = movieId;
			this.userId = userId;
			this.rating = rating;
		}
	}

	/**
	 * Turns a line from the input file into a DataRow object (used for both
	 * Training and Testing data).
	 * 
	 * @param line
	 * @throws IOException
	 */
	static DataRow parseLine(String line) throws IOException {
		String[] parts = line.split(",");
		if (parts.length != 3) {
			throw new IOException("Line: " + line + " in bad form!");
		}
		return new DataRow(Short.parseShort(parts[0]), Integer
				.parseInt(parts[1]), (byte) Float.parseFloat(parts[2]));
	}

	/**
	 * Reads the contents of a text file in the format provided for the project
	 * (e.g. "TestingRatings.txt"), creates a MemStore, populates it with this
	 * data, and returns it.
	 */
	public MemStore getPopulatedStore(String filename) throws IOException {
		MemStore mem = new MemStore();
		BufferedReader in = readFile(filename);
		String line;
		int i = 0;
		long start = System.currentTimeMillis();
		Set<Short> movieIds = new HashSet<Short>();
		Set<Integer> userIds = new HashSet<Integer>();
		while ((line = in.readLine()) != null) {
			DataRow dr = parseLine(line);
			movieIds.add(dr.movieId);
			userIds.add(dr.userId);
			mem.addRating(dr.movieId, dr.userId, dr.rating);
			if (++i % 10000 == 0) {
				System.err.println("Passed " + i + " datapoints in "
						+ (System.currentTimeMillis() - start) / 1000
						+ " seconds");
			}
		}
		System.err.println("Have " + movieIds.size() + " movies and "
				+ userIds.size() + " users");
		i = 0;
		for (EntityType type : EntityType.values()) {
			System.err.println("Initializeing " + type.toString());
			for (Ratings r : mem.getMap(type).values()) {
				r.init();
				System.err.print(".");
			}
			System.err.println();
		}
		return mem;
	}
	
	/**
	 * Reads the contents of a file into a BufferedReader.
	 * @param filename
	 * @throws IOException
	 */
	public static BufferedReader readFile(String filename) throws IOException {
		File f = new File(filename);
		if (!f.exists()) {
			throw new IOException("Couldn't locate file " + f);
		}
		return new BufferedReader(new FileReader(f));
	}

}
