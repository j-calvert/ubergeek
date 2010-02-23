package povgen;

import java.io.IOException;


public class CameraMan extends PovBase {

	public static void main(String[] args) throws IOException {
		Ac3d.parse("render.pov");
		new CameraMan().shootMovie();
		new CameraMan().shootMovie2();
	}
		
	public void shootMovie() throws IOException {
		Vec total = new Vec(0, 400, 0);
		Vec spools = new Vec(0, 370, 230);
		Vec sa = new Vec(0, 250, -50);
		Vec mid = new Vec(0, 130, 85);
		circle(total, 150, 400, 4);
		transition(spools, new Vec(spools.x- 200, spools.y + 50, spools.z), 3);
		circle(spools, 50, 200, 4);
		transition(sa, new Vec(sa.x- 200, sa.y + 50, sa.z), 3);
		circle(sa, 50, 200, 4);
		transition(mid, new Vec(mid.x- 200, mid.y + 50, mid.z), 3);
		circle(mid, 50, 200, 4);
		transition(new Vec(0, 200, 0), new Vec(-400, 190, 0), 2);
	}
	
	public void shootMovie2() throws IOException {
		camLoc = new Vec(-400, 190, 0);
		camLook = new Vec(0, 200, 0);
		int start = frame;
		while(frame - start < 200) {
			pivotAngle = 30 * Math.sin(2 * Math.PI * (double) frame / 100);
			shootFramePivoting();
			frame++;
		}
	}
	
	
	


	private void shootFrame() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		print("Grass_Field");
		print("Sky_With_Coulds");
		
		CompSet.steeringFloating.print();
		snap(formatter.format("cameraMan" + frame));
		System.out.println(camLoc + " " + camLook);
	}
	
	private void shootFramePivoting() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		print("Grass_Field");
		print("Sky_With_Coulds");
		
		print("union{");
		CompSet.steeringFloating.print();
		print(" rotate<0, " + pivotAngle + ",0>}");
		CompSet.pivotBase.print();
		snap(formatter.format("cameraMan" + frame));
	}
	
	private void circle(Vec focus, double height, double radius, double dur) throws IOException {
		System.out.println("Start circle at frame " + frame);
		camLook = focus;
		camLoc.y = focus.y + height;
		int numFrames = (int) Math.round(dur * FRAMES_PER_SECOND);
		int start = frame;
		while(frame - start < numFrames) {
			camLoc.x = camLook.x -radius * Math.cos(2 * Math.PI * (frame - start) / numFrames);
			camLoc.z = camLook.z - radius * Math.sin(2 * Math.PI * (frame - start) / numFrames);
			shootFrame();
			frame++;
		}
		System.out.println("Done circle at frame " + frame);
	}
	
	private void transition(Vec look2, Vec loc2, double dur) throws IOException {
		System.out.println("Start transition at frame " + frame);
		int numFrames = (int) Math.round(dur * FRAMES_PER_SECOND);
		int start = frame;
		Vec loc1 = camLoc;
		Vec look1 = camLook;
		while(frame - start <= numFrames) {
			camLoc = loc1.ave(loc2, ((double) frame - start) / numFrames);
			camLook = look1.ave(look2, ((double) frame - start) / numFrames);
			shootFrame();
			frame++;
		}
		System.out.println("End transition at frame " + frame);
	}

}
