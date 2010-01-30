import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Alarm extends Thread {
	
	@Override
	public void run() {
		float seconds = 0.5f;
		int sampleRate = 8000;
		double frequency = 1000.0;
		double RAD = 2.0 * Math.PI;
		for (int j = 0; j < 100; j++) {
			try {
				AudioFormat af = new AudioFormat((float) sampleRate, 8, 2,
						true, true);
				DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
				SourceDataLine source = (SourceDataLine) AudioSystem
						.getLine(info);
				source.open(af);
				source.start();
				byte[] buf = new byte[(int) (sampleRate * seconds)];
				for (int i = 0; i < buf.length; i++) {
					buf[i] = (byte) (Math.sin(RAD * frequency / sampleRate * i) * 127.0);
				}
				source.write(buf, 0, buf.length);
				source.drain();
				source.stop();
				source.close();
				Thread.sleep((int) (700 * seconds));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new Alarm().start();
	}
}
