package edu.washington.csep576;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.sleepycat.persist.model.Persistent;

@Persistent
public class Region {
	
	transient Map<Integer, TreeSet<Integer>> area = new TreeMap<Integer, TreeSet<Integer>>();
	
	private static final long serialVersionUID = 1L;

	double[] blpDist = new double[256];
	
	double[] color = new double[3];
	
	// center x/y, high x/y, low x/y, size respectively
	int cx, cy, hx, hy, lx, ly, size;
	
	public Region(){}
	
	public Region(double[] color){
		this.color = color;
	}
	
	public void computeTexture(PPM ppm){
		for(int x : area.keySet()){
			for(int y : area.get(x)){
				if(containsNeighborhood(x, y, 2)){
					int s = computeBlp(ppm, x, y);
					if(s > 255){
						computeBlp(ppm, x, y);
					}
					blpDist[s]++;
				}
			}
		}
		// normalize
		double total = 0;
		for(double d : blpDist){
			total += d;
		}
		if(total > 0){
			for(int i = 0; i < blpDist.length ; i++){
				blpDist[i] = blpDist[i] / total;
			}
		}
	}
	
	public void computeGeometry(){
		long tx = 0, ty = 0;
		hx = Integer.MIN_VALUE;
		hy = Integer.MIN_VALUE;
		lx = Integer.MAX_VALUE;
		ly = Integer.MAX_VALUE;
		for(int x : area.keySet()){
			if(x < lx){ lx = x; }
			if(x > hx){ hx = x; }
			for(int y : area.get(x)){
				if(y < ly){ ly = y; }
				if(y > hy){ hy = y; }
				tx += x;
				ty += y;
				size++;
			}
		}
		cx = (int) Math.round(tx * 1d / size);
		cy = (int) Math.round(ty * 1d / size);
	}
	
	private boolean containsNeighborhood(int x, int y, int rad){
		for(int i = x - rad; i <= x + rad; i++){
			if(!area.containsKey(i)){ return false; }
			for(int j = y - rad; j <= y + rad; j++){
				if(!area.get(i).contains(j)){ return false; }
			}
		}
		return true;
	}
	
	private static final double oosr2 = 1d / Math.sqrt(2);
	private static int computeBlp(PPM ppm, int x, int y){
		double center = ppm.getIntensity(x, y);
		int s = 0;
		if(ppm.getIntensity(x + 1, y) > center){
			s += 1;
		}
		// hand coded bilinear interpolation
		double d = ppm.getIntensity(x + 1, y + 1) * 0.5;
		d += ppm.getIntensity(x, y + 1) * oosr2 * (1d - oosr2);
		d += ppm.getIntensity(x + 1, y) * oosr2 * (1d - oosr2);
		d += center * (1d - oosr2) * (1d - oosr2);
		if(d > center){
			s += 2;
		}
		if(ppm.getIntensity(x, y + 1) > center){
			s += 4;
		}
		// hand coded bilinear interpolation
		d = ppm.getIntensity(x - 1, y + 1) * 0.5;
		d += ppm.getIntensity(x, y + 1) * oosr2 * (1d - oosr2);
		d += ppm.getIntensity(x - 1, y) * oosr2 * (1d - oosr2);
		d += center * (1d - oosr2) * (1d - oosr2);
		if(d > center){
			s += 8;
		}
		if(ppm.getIntensity(x - 1, y) > center){
			s += 16;
		}
		// hand coded bilinear interpolation
		d = ppm.getIntensity(x - 1, y - 1) * 0.5;
		d += ppm.getIntensity(x, y - 1) * oosr2 * (1d - oosr2);
		d += ppm.getIntensity(x - 1, y) * oosr2 * (1d - oosr2);
		d += center * (1d - oosr2) * (1d - oosr2);
		if(d > center){
			s += 32;
		}
		if(ppm.getIntensity(x, y - 1) > center){
			s += 64;
		}
		// hand coded bilinear interpolation
		d = ppm.getIntensity(x + 1, y - 1) * 0.5;
		d += ppm.getIntensity(x, y - 1) * oosr2 * (1d - oosr2);
		d += ppm.getIntensity(x + 1, y) * oosr2 * (1d - oosr2);
		d += center * (1d - oosr2) * (1d - oosr2);
		if(d > center){
			s += 128;
		}
		return s;
	}
}
