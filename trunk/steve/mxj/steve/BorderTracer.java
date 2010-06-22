package steve;

public class BorderTracer extends EdgeTracer {
	
	
	@Override
	protected void getTraces(int w, int h, int[] data) {		
		// TODO Auto-generated method stub
		int[] newData = new int[data.length];
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++) {
				if(onBorder(data, w, h, x, y)) {
					newData[x + y * w] = data[x + y * w];
				} else {
					newData[x + y * w] = 0;
				}
			}
		}
		super.getTraces(w, h, newData);
	}

	private boolean onBorder(int[] data, int w, int h, int x, int y) {
		int c = getPixel(data, w, x, y);
		if (c == 0)
			return false;
		int sCount = 0;
		int tCount = 0;
		for (int i = 0; i < nbrs.length; i++) {
			int xn = x + nbrs[i][0];
			int yn = y + nbrs[i][1];
			if (xn < 0 || yn < 0 || xn >= w || yn >= h) {
				continue;
			}
			tCount++;
			if (getPixel(data, w, xn, yn) == c) {
				sCount++;
			}
		}
		return sCount < 8 && sCount >= 2;
	}

}
