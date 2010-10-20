package edu.washington.csep576;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import Acme.Serve.Serve;

public class UI extends Serve {
	private static final long serialVersionUID = 1L;

	public UI() {
		Properties properties = new Properties();
		properties.put("port", 8765);
		properties.setProperty(ARG_NOHUP, "nohup");
		arguments = properties;
		PathTreeDictionary alias = new PathTreeDictionary();
		alias.put("/", new File("images/thumbs").getAbsoluteFile());
		setMappingTable(alias);
		this.addDefaultServlets(null);
		this.addServlet("/ui", new UIServlet());
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					notifyStop();
				} catch (IOException ioe) {

				}
				destroyAllServlets();
			}
		}));
		serve();
	}
}
