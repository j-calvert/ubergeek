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
		circle(total, 150, 400, 3);
		transition(total, spools, 150, 50, 400, 200, 2);
		circle(spools, 50, 200, 3);
		transition(spools, sa, 50, 50, 200, 200, 2);
		circle(sa, 50, 200, 3);
		transition(sa, mid, 50, 50, 200, 200, 2);
		circle(mid, 50, 200, 3);
		transition(mid, new Vec(0, 200, 0), new Vec(mid.x-200, mid.y + 50, mid.z), new Vec(-400, 190, 0), 2);
	}
	
	public void shootMovie2() throws IOException {
		camLoc = new Vec(-400, 190, 0);
		camLook = new Vec(0, 200, 0);
		while(frame < 200) {
			pivotAngle = 30 * Math.cos(2 * Math.PI * (double) frame / 100);
			shootFramePivoting();
			frame++;
		}
	}
	
	
	


	private void shootFrame() throws IOException {
//		newFrame();
//		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
//		print(light(camLoc, vec(.2,.2,.2)));
//		print(camera(camLoc, camLook, camAngle));
//		pipe("declare.pov");
//		pipe("sky2.pov");
//		
//		CompSet.steeringFloating.print();
//		snap(formatter.format(frame));
		System.out.println(camLoc + " " + camLook);
	}
	
	private void shootFramePivoting() throws IOException {
		newFrame();
		print(light(vec(-2000, 2500, 500), vec(1,1,1)));
		print(light(camLoc, vec(.2,.2,.2)));
		print(camera(camLoc, camLook, camAngle));
		pipe("declare.pov");
		pipe("sky2.pov");
		
		print("union{");
		CompSet.steeringFloating.print();
		print(" rotate<0, " + pivotAngle + ",0>}");
		CompSet.pivotBase.print();
		snap(formatter.format(frame));
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
	
	private void transition(Vec f1, Vec f2, double h1, double h2, double r1, double r2, double dur) throws IOException {
		Vec c1 = new Vec(f1.x-r1, f1.y + h1, f1.z);
		Vec c2 = new Vec(f2.x-r2, f2.y + h1, f2.z);
		transition(f1, f2, c1, c2, dur);
		
	}

	private void transition(Vec f1, Vec f2, Vec c1, Vec c2, double dur) throws IOException {
		System.out.println("Start transition at frame " + frame);
		int numFrames = (int) Math.round(dur * FRAMES_PER_SECOND);
		int start = frame;
		while(frame - start <= numFrames) {
			camLoc = c1.ave(c2, ((double) frame - start) / numFrames);
			camLook = f1.ave(f2, ((double) frame - start) / numFrames);
			shootFrame();
			frame++;
		}
		System.out.println("End transition at frame " + frame);
	}

}
