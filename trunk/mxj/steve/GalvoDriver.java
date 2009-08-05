package steve;
import com.cycling74.jitter.JitterMatrix;
import com.cycling74.max.DataTypes;
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;

public class GalvoDriver extends MSPPerformer {

    private static final String[] INLET_ASSIST = new String[] { "messages in" };

    private static final String[] OUTLET_ASSIST = new String[] { "x out", "y out", "blanking out" };
    
    private float step = .0001f;
    private float c = 0.0f;

    public GalvoDriver() {
        declareInlets(new int[] { DataTypes.ALL });
        declareOutlets(new int[] { SIGNAL, SIGNAL, SIGNAL });

        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);
        declareAttribute("step");
    }
    
    JitterMatrix jm = new JitterMatrix();
    boolean ready = true;
    int scale;
    public void jit_matrix(String s) {
		if (ready) {
			jm.frommatrix(s);
			int dim[] = jm.getDim();
			int[] data = new int[dim[0] * dim[1]];
			scale = Math.max(dim[0], dim[1]);
			jm.copyMatrixToArray(data);
			ready = false;
			ready = true;
		}
	}

    @Override
    public void perform(MSPSignal[] in, MSPSignal[] out) {
        for (int i = 0; i < out[0].vec.length; i++) {
        	c += step;
            out[0].vec[i] = (float) (5.0 * Math.cos(c));
            out[1].vec[i] = (float) (5.0 * Math.sin(c));
            out[2].vec[i] = (float) (5.0 * Math.cos(c));
        }
    }

}
