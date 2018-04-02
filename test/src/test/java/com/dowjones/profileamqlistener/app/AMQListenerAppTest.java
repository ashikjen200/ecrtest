/**
 * 
 */
package com.dowjones.profileamqlistener.app;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.PropertyResourceBundle;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.dowjones.contrail.client.exception.ContrailException;
import com.dowjones.contrail.client.util.ContrailClientConfiguration;

/**
 * @author kumars
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AMQListenerAppTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
            System.setProperty("broker.url", "tcp://SKPCINFYW7VM096:61616");
            System.setProperty("log_level", "info");
	}



	/**
	 * Test method for
	 * {@link com.dowjones.profileamqlistener.config.AMQListenerApp#main(java.lang.String[])}.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ContrailException
	 */
	@Test
	public final void testMainWithProperties() throws ContrailException, IOException, URISyntaxException {
		AMQListenerApp.main("src/main/resources/application.properties");
		PropertyResourceBundle p = ContrailClientConfiguration.getInstance().getProps();

		assertEquals(p.getString("contrailMessageSchemaPath"), "src/main/resources/ContrailMessage.xsd");
		assertEquals(p.getString("agentTimeBetweenChecks"), "10000");

	}
	
//	/**
//	 * Test method for
//	 * {@link com.dowjones.profileamqlistener.config.AMQListenerApp#main(java.lang.String[])}.
//	 * 
//	 * @throws URISyntaxException
//	 * @throws IOException
//	 * @throws ContrailException
//	 */
//	@Test
//	public final void testMainWithoutProperties() throws ContrailException, IOException, URISyntaxException {
//		AMQListenerApp.main();
//		PropertyResourceBundle p = ContrailClientConfiguration.getInstance().getProps();
//
//		assertEquals(p.getString("contrailMessageSchemaPath"), "src/main/resources/ContrailMessage.xsd");
//		assertEquals(p.getString("agentTimeBetweenChecks"), "10000");
//
//	}


}
