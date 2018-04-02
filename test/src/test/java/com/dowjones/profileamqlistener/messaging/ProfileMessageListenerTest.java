/**
 * 
 */
package com.dowjones.profileamqlistener.messaging;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;

import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.dowjones.contrail.client.service.commit.ICommit;
import com.dowjones.contrail.common.logger.FactivaLogger;
import com.dowjones.contrail.common.message.ContrailMessage;
import com.dowjones.profileamqlistener.service.KinesisRecordsWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author kumars
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileMessageListenerTest {

	/**
	 * @throws java.lang.Exception
	 */

	@Mock
	private KinesisRecordsWriter kinesisRecordWriter;
	private String payload;
	private File payloadFile = new File("src/test/resources/ContentDelivery.xml");

	@Before
	public void setUp() throws Exception {
		payload = new String(Files.readAllBytes(payloadFile.toPath().toAbsolutePath()), "UTF8");
		FactivaLogger.getLogger("", "src/main/resources/log4j.xml");

		PutRecordResult kinesisRes = new PutRecordResult();
		//Logs should print : Message written to kinesis stream has seqnuence no:123
		kinesisRes.setSequenceNumber("123");

		when(kinesisRecordWriter.putRecord(any(byte[].class))).thenReturn(kinesisRes);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testProfileMessageListener() throws Exception {

		ICommit iCommit = createContrailMessage();
		ProfileMessageListener msgListener = new ProfileMessageListener(kinesisRecordWriter);
		
		msgListener.processMessage(iCommit);

	}
	
	@Test(expected = XMLStreamException.class)
	public void testXMLStreamException() throws JsonProcessingException, XMLStreamException {
		ProfileMessageListener msgListener = mock(ProfileMessageListener.class);
		doThrow(new XMLStreamException("Error parsing payload:"))
	      .when(msgListener)
	      .parsePayloadToJSON(any(ContrailMessage.class));
	 
		ContrailMessage message = mock(ContrailMessage.class);
		msgListener.parsePayloadToJSON(message);
	}
	
	

	private ICommit createContrailMessage() throws Exception {

		ContrailMessage message = new ContrailMessage();
		message.setPayloadData("ContentDelivery", payload);
		ICommit iCommit = mock(ICommit.class);
		when(iCommit.getContrailMessage()).thenReturn(message);
		return iCommit;
	}

}
