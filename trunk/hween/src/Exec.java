import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Exec {

    public static void run(String[] command) {
        try {
			Process exec = Runtime.getRuntime().exec(command);
			StreamGobbler errorGobbler = new StreamGobbler(exec.getErrorStream(),
			        "ERROR");

			StreamGobbler outputGobbler = new StreamGobbler(exec.getInputStream(),
			        "OUTPUT");
			errorGobbler.start();
			outputGobbler.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

    }

    // Courtesy of
    // http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
    private static class StreamGobbler extends Thread {
        InputStream is;
        String type;
        OutputStream os;

        StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
            if ("ERROR".equals(type)) {
                os = System.err;
            } else {
                os = System.out;
            }
        }

        public void run() {
            try {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);

                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (pw != null)
                        pw.println(line);
                    System.out.println(type + ">" + line);
                }
                if (pw != null)
                    pw.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}