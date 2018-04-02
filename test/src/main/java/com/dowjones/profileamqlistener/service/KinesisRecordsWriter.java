/**
 * 
 */
package com.dowjones.profileamqlistener.service;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;

/**
 * This class is used to write the record (one at a time as the PCI framework
 * calls back for each record)
 * 
 * @author kumars
 *
 */
public class KinesisRecordsWriter implements RecordWriter {

	private static final Logger LOGGER = LogManager.getLogger(KinesisRecordsWriter.class);

	// Used to connect to Kinesis Data Stream and put records
	private AmazonKinesis kinesisClient = AmazonKinesisClientBuilder.defaultClient();

	// name of the kinesis data stream
	private String kinesisStreamName;

	/**
	 * 
	 * 
	 * @param kinesisStreamName
	 */
	public KinesisRecordsWriter(String kinesisStreamName) {
		this.kinesisStreamName = kinesisStreamName;
	}
	
	public KinesisRecordsWriter(String kinesisStreamName, AmazonKinesis kinesisClient) {
		this.kinesisStreamName = kinesisStreamName;
		this.kinesisClient = kinesisClient;
	}

	/*
	 * This method is used to write records to the KinesisDataStream
	 * 
	 * @see com.dowjones.profileamqlistener.service.RecordWriter#putRecords()
	 */
	@Override
	public PutRecordResult putRecord(byte[] payload) throws Exception {
		LOGGER.info("Writing the record to kinesisstream");

		// create a request to put the message in stream
		PutRecordRequest putRecordRequest = new PutRecordRequest().withData(ByteBuffer.wrap(payload))
				.withPartitionKey(UUID.randomUUID().toString()).withStreamName(kinesisStreamName);

		final PutRecordResult putRecordResult = kinesisClient.putRecord(putRecordRequest);

		LOGGER.info("Finished Writing the record to kinesisstream ");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("putRecordResult: " + putRecordResult.toString());
		}
		return putRecordResult;
	}

}
