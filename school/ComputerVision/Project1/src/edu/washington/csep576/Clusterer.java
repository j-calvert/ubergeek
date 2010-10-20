package edu.washington.csep576;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * The class that parses up command-line parameters, creates the PPM, runs the
 * k-means clusterer on it, and outputs artifacts (in the same directory with
 * suffixes)
 */
public class Clusterer {

	public static void main(String[] args) {
		try {
			if(args.length == 0){
				usage();
				return;
			}
			String filename = args[0];
			int maxClusters = 9;
			if (args.length > 1) {
				maxClusters = Integer.parseInt(args[1]);
			}
			int initialClusters = maxClusters;
			if (args.length > 2) {
				initialClusters = Integer.parseInt(args[2]);
			}
			boolean converted = false;
			boolean selected = false;
			for (int i = 3; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("converted")) {
					converted = true;
				} else if (args[i].equalsIgnoreCase("selected")) {
					selected = true;
				}
			}
			double mergeFactor = 3.1d;
			File f = new File(filename);
			FileInputStream fis = new FileInputStream(f);
			PPM pic = new PPM(fis);
			if (converted) {
				pic.rgb2yiq();
			}
			Kmeans kmeans = new Kmeans(pic, mergeFactor, maxClusters);
			if (selected) {
				kmeans.selectedSeeds(initialClusters);
			} else {
				kmeans.randomSeeds(initialClusters);
			}
			kmeans.clusterColors();
			kmeans.doMask();
			if (converted) {
				pic.yiq2rgb();
			}
			File g = new File(f.toString() + ".mask");
			File h = new File(f.toString() + ".spec");
			kmeans.getSpectrum().write(new FileOutputStream(h));
			FileOutputStream fos = new FileOutputStream(g);
			pic.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
			usage();
		}
	}

	public static void usage() {
		System.err
				.println("-----------------------------------------------------------------------------------------------");
		System.err.println("Usage:");
		System.err
				.println("java com/washington/csep576/Clusterer <inputFile> [<maxClusters> [<initialClusters> [options]]]");
		System.err
				.println("where: inputFile is the PPM input file...the output file will be placed in the same directory with the string 'mask' inserted just before the suffix");
		System.err
				.println("       maxClusters is an integer number of max clusters, default 10");
		System.err
				.println("       initialClusters is an integer number of initial clusters (default maxClusters)");
		System.err.println(" and options can be ");
		System.err
				.println("       'converted' indicating that image should be converted to YIQ space for clustering");
		System.err
				.println("       'selective' indicating that seeds should be picked so they're far from one another");
	}

}
