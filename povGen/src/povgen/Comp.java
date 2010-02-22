package povgen;

import java.awt.Color;


public enum Comp {
	AnchoredFrame(Color.GRAY, Finish.Polished),
	Generator(Color.MAGENTA, Finish.Brass),
	RedSteerinChain(Color.RED, Finish.Metal),
	AnchoredPivotBase(Color.DARK_GRAY, Finish.Soft_Silver),
	GeneratorAxle(Color.MAGENTA, Finish.Brass),
	SteeringSAAxle(Color.GRAY, Finish.Brass),
	AnchoredPivotBasePulley(Color.GREEN, Finish.Brass),
	GeneratorChain(Color.ORANGE, Finish.Metal),
	SteeringSABlueChainwheel(Color.BLUE, Finish.Metallic),
	BlueSteeringChain(Color.BLUE, Finish.Metal),
	GeneratorChainwheel(Color.MAGENTA, Finish.Metallic),
	SteeringSANubRed(Color.RED, Finish.Chrome),
	CogHub(Color.GREEN, Finish.Metallic),
	GeneratorCogChainwheel(Color.ORANGE, Finish.Metallic),
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
	FlywheelSAChainwheel(Color.ORANGE, Finish.Metallic),
	RecoilSAChainwheel(Color.GREEN, Finish.Metallic);

//	AnchoredFrame("ABABAB", Finish.Polished),
//	Generator("A10527", Finish.Brass),
//	RedSteerinChain("CF6969", Finish.Metal),
//	AnchoredPivotBase("585859", Finish.Soft_Silver),
//	GeneratorAxle("961719", Finish.Brass),
//	SteeringSAAxle("E3E33D", Finish.Brass),
//	AnchoredPivotBasePulley("7FE376", Finish.Brass),
//	GeneratorChain("9C1618", Finish.Metal),
//	SteeringSABlueChainwheel("7272DB", Finish.Metallic),
//	BlueSteeringChain("7769CF", Finish.Metal),
//	GeneratorChainwheel("AD2A2C", Finish.Metallic),
//	SteeringSANubRed("D42424", Finish.Chrome),
//	CogHub("71BA6A", Finish.Metallic),
//	GeneratorCogChainwheel("961719", Finish.Metallic),
//	SteeringSARedChainWheel("D43131", Finish.Metallic),
//	DRSAAxle("56CC4B", Finish.Brass),
//	GeneratorSAChainwheel("AD2A2C", Finish.Metallic),
//	SteeringSpoolBlue("0202A3", Finish.Chrome),
//	DRSACasing("986EB8", Finish.Polished),
//	KiteLineBlue("4444C7", Finish.Brass),
//	SteeringSpoolChainwheelBlue("7272DB", Finish.Metallic),
//	DRSAPlanetNub("8C1130", Finish.Silver),
//	KiteLineRed("F50505", Finish.Brass),
//	SteeringSpoolChainwheelRed("D43131", Finish.Metallic),
//	DRSpoolAxle("ABABAB", Finish.Metal),
//	MiddleSpool("71BA6A", Finish.Chrome),
//	SteeringSpoolRed("A10000", Finish.Chrome),
//	DRSpoolChainwheel("71BA6A", Finish.Metallic),
//	MiddleSpoolAxle("ABABAB", Finish.Brass),
//	SteeringSpoolsAxle("ABABAB", Finish.Brass),
//	DRSpoolRear("71BA6A", Finish.Chrome),
//	MiddleSpoolBlueChainwheel("7272DB", Finish.Metallic),
//	SteeringWheel("E3E33D", Finish.Soft_Silver),
//	Flywheel("986EB8", Finish.Metal),
//	MiddleSpoolRedChainwheel("D43131", Finish.Metallic),
//	SteerningSAHubBlue("8080C4", Finish.Polished),
//	FlywheelAxle("ABABAB", Finish.Brass),
//	PivotFrame("ABABAB", Finish.Polished),
//	TransmissionChain("36A32C", Finish.Metal),
//	TransmissionCogAxle("7FE376", Finish.Brass),
//	FlywheelChainwheel("7C55C9", Finish.Metallic),
//	RecoilCogChainwheel("71BA6A", Finish.Metallic),
//	TransmissionLine("7FE376", Finish.Brass),
//	FlywheelSAChainwheel("7C55C9", Finish.Metallic),
//	RecoilSAChainwheel("71BA6A", Finish.Metallic);

	public Color color;
	public final Finish finish;
	
	private Comp(Color color, Finish finish){
		this.color = color;
		this.finish = finish;
	}
	
	private Comp(String color, Finish finish) {
		this.color = Color.decode("0x" + color);
		this.finish = finish;
	}
	
}
