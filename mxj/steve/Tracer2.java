package steve;

import java.util.ArrayList;
import java.util.List;

import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.DataTypes;
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;

public abstract class Tracer2 extends MSPPerformer {
    JitterMatrix jm = new JitterMatrix();

    protected static final int[][] nbrs = new int[][] { { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 },
            { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 } };

    List<List<Coord>> readTraces = new ArrayList<List<Coord>>();
    List<List<Coord>> writeEdges = new ArrayList<List<Coord>>();

    protected Tracer2() {
        declareInlets(new int[] { DataTypes.ALL });
        declareOutlets(new int[] { SIGNAL, SIGNAL, SIGNAL });

        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);
        declareAttribute("spp");
        declareAttribute("scale");
        List<Coord> initiEdge = new ArrayList<Coord>();
        initiEdge.add(new Coord(0, 0));
        readTraces.add(initiEdge);
        advanceFrame();
    }


    private int edgeIndex;
    private int pointIndex;
    private int spp = 10;
    private float scale = 100;

    private int stallIndex;
    private boolean ready = true;

    public void jit_matrix(String s) {
        if (ready) {
            jm.frommatrix(s);
            int dim[] = jm.getDim();
            int[] data = new int[dim[0]];
            jm.copyMatrixToArray(data);
            ready = false;
            getTraces(data);
            advanceFrame();
            ready = true;
        }
    }

    private void advanceFrame() {
        // post("advancing frame with this many edges: " + readEdges.size());
        writeEdges = readTraces;
        resetIndexes();
        readTraces = new ArrayList<List<Coord>>();
    }

    private void resetIndexes() {
        this.edgeIndex = 0;
        this.pointIndex = 0;
        this.stallIndex = 0;
    }

    protected abstract void getTraces(int[] data);

    protected int getPixel(int[] data, int w, int i, int j) {
        return data[i + j * w];
    }

    protected class Coord {
        int x, y;

        Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Coord) {
                if (((Coord) obj).x == x && ((Coord) obj).y == y) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return x + y * 640;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        public int intDist(Coord c) {
            int dx = x - c.x;
            int dy = y - c.y;
            return (int) Math.min(Math.sqrt(dx * dx + dy * dy), 1);
        }

    }

    // / MAX Stuff
    private static final String[] INLET_ASSIST = new String[] { "messages in" };

    private static final String[] OUTLET_ASSIST = new String[] { "x out", "y out", "blanking out" };

    @Override
    public void perform(MSPSignal[] in, MSPSignal[] out) {
        // post("performing! " + stallIndex + ", " + pointIndex + ", " + edgeIndex);
        for (int i = 0; i < out[0].vec.length; i++) {
            try {
                if (stallIndex >= spp) {
                    stallIndex = 0;
                    pointIndex++;
                } else {
                    stallIndex++;
                }
                if (pointIndex >= this.writeEdges.get(edgeIndex).size()) {
                    edgeIndex++;
                    pointIndex = 0;
                    stallIndex = 0;
                }
                if (edgeIndex >= this.writeEdges.size()) {
                    edgeIndex = 0;
                    pointIndex = 0;
                    stallIndex = 0;
                }
                out[0].vec[i] = (float) (this.writeEdges.get(edgeIndex).get(pointIndex).x
                        * (2.0 / scale) - 1.0);
                out[1].vec[i] = (float) (this.writeEdges.get(edgeIndex).get(pointIndex).y
                        * (2.0 / scale) - 1.0);
                out[2].vec[i] = (blank(pointIndex));
            } catch (Exception e) {
                // OOB and NPE possible cos of race condition
                // post("exception:" + e.getMessage());
                // post(edgeIndex + ": " + pointIndex + ": " + stallIndex);
                resetIndexes();
            }
        }
    }

    private static final int LAS_OFF = 1;
    private static final int LAS_ON = 0;

    private float blank(int pointIndex) {
        return pointIndex == 0 ? LAS_OFF : LAS_ON;
    }
}
