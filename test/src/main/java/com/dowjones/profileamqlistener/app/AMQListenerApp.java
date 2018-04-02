package com.dowjones.profileamqlistener.app;

import java.io.IOException;
import java.net.URISyntaxException;

import com.dowjones.contrail.client.exception.ContrailException;
import com.dowjones.contrail.client.util.ContrailClientConfiguration;

/**
 * This is the starting point for the Profiles AMQ Listener A default
 * application.properties resource class has been supplied with it but, if you
 * can override that by supplying an accessible path to an external properties
 * file.
 * 
 * e.g. java -jar jar_name [full_path_to_peoprties_file]
 * Also, need to set Java environment variables
 * 
 * @author kumars
 *
 */
public class AMQListenerApp {

	// path in the classpath where the default property file is present
	private static final String PROPERTIES_PATH = "src/main/resources/application.properties";

	/**
	 * This is the starting point for the AMQ Listener. This method initializes the
	 * PCI framework and spring context. 
	 * @param args
	 * @throws ContrailException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String... args) throws ContrailException, IOException, URISyntaxException {
		// It makes sure to use default configuration if an external properties file in
		// not supplied
		if (args.length == 0) {
			ContrailClientConfiguration.getInstance(PROPERTIES_PATH, false);
		} else {
			ContrailClientConfiguration.getInstance(args[0], false);
		}

		// Start the spring application context
		ContrailClientConfiguration.getInstance().startContext();
	}

}
