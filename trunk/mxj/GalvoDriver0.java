import com.cycling74.max.DataTypes;
import com.cycling74.msp.MSPPerformer;
import com.cycling74.msp.MSPSignal;

public class GalvoDriver0 extends MSPPerformer {

    private static final String[] INLET_ASSIST = new String[] { "messages in" };

    private static final String[] OUTLET_ASSIST = new String[] { "x out", "y out", "blanking out" };

    private static final double PI = Math.PI;

    public GalvoDriver0() {
        declareInlets(new int[] { DataTypes.ALL });
        declareOutlets(new int[] { SIGNAL, SIGNAL, SIGNAL });

        setInletAssist(INLET_ASSIST);
        setOutletAssist(OUTLET_ASSIST);

    }

    @Override
    public void perform(MSPSignal[] in, MSPSignal[] out) {
        for (int i = 0; i < out[0].vec.length; i++) {
            out[0].vec[i] = (float) (Math.cos(i / 32 * PI));
            out[1].vec[i] = (float) (Math.sin(i / 32 * PI));
        }
    }

}
