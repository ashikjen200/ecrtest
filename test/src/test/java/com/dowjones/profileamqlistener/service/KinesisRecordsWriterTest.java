/**
 * 
 */
package com.dowjones.profileamqlistener.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.dowjones.contrail.common.logger.FactivaLogger;

/**
 * @author kumars
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class KinesisRecordsWriterTest {

	private String payload;
	private File payloadFile = new File("src/test/resources/ContentDelivery.xml");
	AmazonKinesisClient kinesisClient = mock(AmazonKinesisClient.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		payload = new String(Files.readAllBytes(payloadFile.toPath().toAbsolutePath()), "UTF8");
		FactivaLogger.getLogger("", "src/main/resources/log4j.xml");

		when(kinesisClient.putRecord(any(PutRecordRequest.class))).then(new Answer<PutRecordResult>() {
			@Override
			public PutRecordResult answer(InvocationOnMock invocation) throws Throwable {
				return answerKinesisPutRecord(invocation);
			}
		});

	}

	protected PutRecordResult answerKinesisPutRecord(InvocationOnMock invocation) {
		PutRecordRequest request = (PutRecordRequest) invocation.getArguments()[0];

		assertEquals(request.getData(), ByteBuffer.wrap(payload.getBytes()));
		PutRecordResult result = new PutRecordResult();
		result.setSequenceNumber("123");
		result.setShardId("shard-01");
		return result;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testPutRecord() throws Exception {
		RecordWriter kinesisRecordsWriter = new KinesisRecordsWriter("teststream", kinesisClient);
		PutRecordResult result = kinesisRecordsWriter.putRecord(payload.getBytes());
		assertEquals(result.getShardId(), "shard-01");

	}

}
