package com.dowjones.profileamqlistener.service;

import com.amazonaws.services.kinesis.model.PutRecordResult;

/**
 * This interface is used to write the records to AWS services(Kinesis)
 * 
 * @author kumars
 *
 */
public interface RecordWriter {
	
	/*
	 * This is used to put one message at a time to the given
	 * Kinesis Data Stream
	 */
	PutRecordResult putRecord(byte[] payload) throws Exception;

}
