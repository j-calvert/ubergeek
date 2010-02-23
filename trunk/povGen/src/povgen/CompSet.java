package povgen;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CompSet extends PovBase {

	Set<Comp> comps = new HashSet<Comp>();
	Set<CompSet> children = new HashSet<CompSet>();

	public static CompSet steeringFloating = new CompSet();
	public static CompSet pivotBase = new CompSet();
	static {
		steeringFloating.comps.add(Comp.BlueSteeringChain);
		steeringFloating.comps.add(Comp.KiteLineBlue);
		steeringFloating.comps.add(Comp.KiteLineRed);
		steeringFloating.comps.add(Comp.MiddleSpool);
		steeringFloating.comps.add(Comp.MiddleSpoolAxle);
		steeringFloating.comps.add(Comp.MiddleSpoolBlueChainwheel);
		steeringFloating.comps.add(Comp.MiddleSpoolRedChainwheel);
		steeringFloating.comps.add(Comp.PivotFrame);
		steeringFloating.comps.add(Comp.RedSteerinChain);
		steeringFloating.comps.add(Comp.SteeringSAAxle);
		steeringFloating.comps.add(Comp.SteeringSABlueChainwheel);
		steeringFloating.comps.add(Comp.SteeringSANubRed);
		steeringFloating.comps.add(Comp.SteeringSARedChainWheel);
		steeringFloating.comps.add(Comp.SteeringSpoolBlue);
		steeringFloating.comps.add(Comp.SteeringSpoolChainwheelBlue);
		steeringFloating.comps.add(Comp.SteeringSpoolChainwheelRed);
		steeringFloating.comps.add(Comp.SteeringSpoolRed);
		steeringFloating.comps.add(Comp.SteeringSpoolsAxle);
		steeringFloating.comps.add(Comp.SteeringWheel);
		steeringFloating.comps.add(Comp.SteerningSAHubBlue);
		
		pivotBase.comps.add(Comp.AnchoredPivotBase);
	}

	public void print() throws IOException {
		Set<Comp> dedup = new HashSet<Comp>();
		dedup.addAll(comps);
		for (CompSet child : children) {
			dedup.addAll(child.comps);
		}
		for (Comp c : dedup) {
			c.print();
		}
		
		
	}

	
	
	public static CompSet all() {
		CompSet all = new CompSet();
		for(Comp c : Comp.values()) {
			all.comps.add(c);
		}
		return all;
	}
}
