package povgen;

import java.io.IOException;


public class CameraStill extends PlanetarySteeringMovie {

	public static void main(String[] args) throws IOException {
//		Ac3d.parse("render.pov");
		CameraStill still = new CameraStill();
		still.camLoc = vec(-600, 400, -130);
		still.camLook = vec(0, 400, -100);		
//		Total perspective
//		still.camLoc = vec(-300, 600, 600);
//		still.camLook = vec(0, 300, 0);
		still.shootFrame();
	}
			
	private void shootFrame() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 200), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		pipe("sceneGrassyField.pov");
		
//		print("union {");
//		pov(Comp.SteeringSARedChainWheel, Comp.SteeringSABlueChainwheel, Comp.SteeringSAAxle);
//		Vec translate = vec(0, 250.4, -47.6924);
//		print(" rotate <0, 0, 0> translate " + translate + "}");
//		Comp.PivotFrame.print();
//		Comp.SteeringSAAxle.print();
//		Comp.SteeringWheel.print();


//		print("union {");
//		pov(Comp.Flywheel, Comp.Generator, Comp.DRSAAxle);
//		legend(15, Comp.Flywheel, Comp.Generator, Comp.DRSAAxle);
//		Vec translate = vec(0, 111.52, -697.795);
//		print(" rotate <0, 180, 0> translate " + translate + "}");

		
		CompSet allAc3d = CompSet.allAc3d();
//		allAc3d.comps.remove(Comp.SteeringSARedChainWheel);
//		allAc3d.comps.remove(Comp.SteeringSABlueChainwheel);
//		allAc3d.comps.remove(Comp.SteeringSANubRed);
//		allAc3d.comps.remove(Comp.SteerningSAHubBlue);
		allAc3d.print();

		snap("still");
		System.out.println(camLoc + " " + camLook);
	}
	
}
	