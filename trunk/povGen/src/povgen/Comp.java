package povgen;

import java.awt.Color;

import povgen.material.Finish;

public enum Comp {
	AnchoredFrame("ABABAB", Finish.Polished),
	Generator("A10527", Finish.Brass),
	RedSteerinChain("CF6969", Finish.Metal),
	AnchoredPivotBase("585859", Finish.Soft_Silver),
	GeneratorAxle("961719", Finish.Brass),
	SteeringSAAxle("E3E33D", Finish.Brass),
	AnchoredPivotBasePulley("7FE376", Finish.Brass),
	GeneratorChain("9C1618", Finish.Metal),
	SteeringSABlueChainwheel("7272DB", Finish.Metallic),
	BlueSteeringChain("7769CF", Finish.Metal),
	GeneratorChainwheel("AD2A2C", Finish.Metallic),
	SteeringSANubRed("D42424", Finish.Chrome),
	CogHub("71BA6A", Finish.Metallic),
	GeneratorCogChainwheel("961719", Finish.Metallic),
	SteeringSARedChainWheel("D43131", Finish.Metallic),
	DRSAAxle("56CC4B", Finish.Brass),
	GeneratorSAChainwheel("AD2A2C", Finish.Metallic),
	SteeringSpoolBlue("0202A3", Finish.Chrome),
	DRSACasing("986EB8", Finish.Polished),
	KiteLineBlue("4444C7", Finish.Brass),
	SteeringSpoolChainwheelBlue("7272DB", Finish.Metallic),
	DRSAPlanetNub("8C1130", Finish.Silver),
	KiteLineRed("F50505", Finish.Brass),
	SteeringSpoolChainwheelRed("D43131", Finish.Metallic),
	DRSpoolAxle("ABABAB", Finish.Metal),
	MiddleSpool("71BA6A", Finish.Chrome),
	SteeringSpoolRed("A10000", Finish.Chrome),
	DRSpoolChainwheel("71BA6A", Finish.Metallic),
	MiddleSpoolAxle("ABABAB", Finish.Brass),
	SteeringSpoolsAxle("ABABAB", Finish.Brass),
	DRSpoolRear("71BA6A", Finish.Chrome),
	MiddleSpoolBlueChainwheel("7272DB", Finish.Metallic),
	SteeringWheel("E3E33D", Finish.Soft_Silver),
	Flywheel("986EB8", Finish.Metal),
	MiddleSpoolRedChainwheel("D43131", Finish.Metallic),
	SteerningSAHubBlue("8080C4", Finish.Polished),
	FlywheelAxle("ABABAB", Finish.Brass),
	PivotFrame("ABABAB", Finish.Polished),
	TransmissionChain("36A32C", Finish.Metal),
	FlywheelChainRecoilChain("36A32C", Finish.Metal),
	TransmissionCogAxle("7FE376", Finish.Brass),
	FlywheelChainwheel("7C55C9", Finish.Metallic),
	RecoilCogChainwheel("71BA6A", Finish.Metallic),
	TransmissionLine("7FE376", Finish.Brass),
	FlywheelSAChainwheel("7C55C9", Finish.Metallic),
	RecoilSAChainwheel("71BA6A", Finish.Metallic);

	public Color color;
	public final Finish finish;
	
	private Comp(String color, Finish finish) {
		this.color = Color.decode("0x" + color);
		this.finish = finish;
	}
	
}
