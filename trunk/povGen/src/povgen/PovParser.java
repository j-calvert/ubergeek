package povgen;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PovParser {

	Map<String, String> comps = new HashMap<String, String>();
	
	public static void main(String[] args) throws IOException {
		new PovParser("render.pov");
		
	}
	
	public PovParser(String pov) throws IOException {
		parse(pov);
		System.out.println();
	}
	
	private void parse(String f1) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader("pov/" + f1));
	
		String s;
		StringBuffer objectPov = new StringBuffer();
		String objectName = null;
		while ((s = in.readLine()) != null) {

			if(s.startsWith("// object ")) {
				objectName = s.replaceFirst("// object ", "");
				objectPov = new StringBuffer();
			} else if (s.startsWith("texture")) {
				// end object
				comps.put(objectName, objectPov.toString());
			} else {
				objectPov.append(s);
			}
		}
		in.close();
	}

}
