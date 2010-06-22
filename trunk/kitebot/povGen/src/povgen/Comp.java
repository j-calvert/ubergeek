package povgen;

import java.awt.Color;
import java.io.IOException;

import povgen.PovBase.Vec;

public enum Comp {
	AnchoredFrame(Color.GRAY, Finish.Polished),
	Generator(Color.MAGENTA, Finish.Brass),
	RedSteerinChain(Color.RED, Finish.Metal),
	AnchoredPivotBase(Color.DARK_GRAY, Finish.Soft_Silver),
	GeneratorAxle(Color.MAGENTA, Finish.Brass),
	SteeringSAAxle(Color.YELLOW, Finish.Brass),
	AnchoredPivotBasePulley(Color.GREEN, Finish.Brass),
	GeneratorChain(Color.MAGENTA, Finish.Metal),
	SteeringSABlueChainwheel(Color.BLUE, Finish.Metallic),
	BlueSteeringChain(Color.BLUE, Finish.Metal),
	GeneratorChainwheel(Color.MAGENTA, Finish.Metallic),
	SteeringSANubRed(Color.RED, Finish.Chrome),
	CogHub(Color.GREEN, Finish.Metallic),
	GeneratorCogChainwheel(Color.MAGENTA, Finish.Metallic),
	SteeringSARedChainWheel(Color.RED, Finish.Metallic),
	DRSAAxle(Color.GREEN, Finish.Brass),
	GeneratorSAChainwheel(Color.MAGENTA, Finish.Metallic),
	SteeringSpoolBlue(Color.BLUE, Finish.Chrome),
	DRSACasing(Color.ORANGE, Finish.Polished),
	KiteLineBlue(Color.BLUE, Finish.Brass),
	SteeringSpoolChainwheelBlue(Color.BLUE, Finish.Metallic),
	DRSAPlanetNub(Color.MAGENTA, Finish.Silver),
	KiteLineRed(Color.RED, Finish.Brass),
	SteeringSpoolChainwheelRed(Color.RED, Finish.Metallic),
	DRSpoolAxle(Color.GRAY, Finish.Metal),
	MiddleSpool(Color.GREEN, Finish.Chrome),
	SteeringSpoolRed(Color.RED, Finish.Chrome),
	DRSpoolChainwheel(Color.GREEN, Finish.Metallic),
	MiddleSpoolAxle(Color.GRAY, Finish.Brass),
	SteeringSpoolsAxle(Color.GRAY, Finish.Brass),
	DRSpoolRear(Color.GREEN, Finish.Chrome),
	MiddleSpoolBlueChainwheel(Color.BLUE, Finish.Metallic),
	SteeringWheel(Color.YELLOW, Finish.Soft_Silver),
	Flywheel(Color.ORANGE, Finish.Metal),
	MiddleSpoolRedChainwheel(Color.RED, Finish.Metallic),
	SteerningSAHubBlue(Color.BLUE, Finish.Polished),
	FlywheelAxle(Color.GRAY, Finish.Brass),
	PivotFrame(Color.GRAY, Finish.Polished),
	TransmissionChain(Color.GREEN, Finish.Metal),
	FlywheelChain(Color.ORANGE, Finish.Metal),
	RecoilChain(Color.GREEN, Finish.Metal),
	TransmissionCogAxle(Color.GRAY, Finish.Brass),
	FlywheelChainwheel(Color.ORANGE, Finish.Metallic),
	RecoilCogChainwheel(Color.GREEN, Finish.Metallic),
	TransmissionLine(Color.GREEN, Finish.Brass),
	TransmissionLinePivot(Color.GREEN, Finish.Brass),
	FlywheelSAChainwheel(Color.ORANGE, Finish.Metallic),
	RecoilSAChainwheel(Color.GREEN, Finish.Metallic),
	Kite(Color.ORANGE, Finish.Brass);

	public Color color;
	public final Finish finish;
	
	private Comp(Color color, Finish finish) {
		this.color = color;
		this.finish = finish;
	}

	public void print() throws IOException {
		PovBase.pipe("parse/" + name(), "", texture() + "}");
	}
	

	public String texture() {
		Vec clr = PovBase.vec(nm(color.getRed()), nm(color.getGreen()),
				nm(color.getBlue()));
		return "texture { pigment{ color rgbf " + clr + "} " + finish + "}";
	}

	public String textureGradient() {
		Vec clr = PovBase.vec(nm(color.getRed()), nm(color.getGreen()),
				nm(color.getBlue()));
		Vec clr2 = clr.scale(0.7);
//		return "texture { pigment{ checker color rgb<1,1,1> color rgb<0,1,0> } " + finish + "}";
		return "texture { pigment{ gradient<0,1,0> color_map{[0.0 color rgb " + clr + "] [0.5 color rgb " + clr + "] [0.5 color rgb " + clr2 + "] [1.0 color rgb " + clr2 + "] } scale <1,10,1> }\n" 
				 + finish + "}";
		}

	private static double nm(int i) {
		double c = ((double) i) / 255;
		return c;
	}
}
