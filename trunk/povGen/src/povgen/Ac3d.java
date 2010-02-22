package povgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Ac3d extends PovBase {


	public static void main(String[] args) throws IOException {		
		parse("render.pov");
	}

	private static void parse(String f1) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));

		String s;
		StringBuffer objectPov = new StringBuffer();
		String objectName = null;
		while ((s = in.readLine()) != null) {

			if (s.startsWith("// object ")) {
				objectName = s.replaceFirst("// object ", "");
				objectPov = new StringBuffer();
			} else if (s.startsWith("texture")) {
				// end object
				new File("pov/parse/" + objectName).delete();
				print(objectPov.toString(), "pov/parse/" + objectName);
			} else {
				objectPov.append(s);
			}
		}
		in.close();
	}

}
