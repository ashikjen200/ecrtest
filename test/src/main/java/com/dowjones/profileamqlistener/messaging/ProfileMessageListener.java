/**
 * 
 */
package com.dowjones.profileamqlistener.messaging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.amazonaws.services.kinesis.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.dowjones.contrail.client.exception.ContrailException;
import com.dowjones.contrail.client.service.commit.ContrailMessageHandler;
import com.dowjones.contrail.client.service.commit.ICommit;
import com.dowjones.contrail.common.message.ContrailMessage;
import com.dowjones.profileamqlistener.service.RecordWriter;
import com.factiva.xml.XMLParser;
import com.factiva.xml.XMLString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

/**
 * This is the class which will receive messages from AMQ topic (Using the PCI
 * Framework)
 * 
 * @author kumars
 *
 */

public class ProfileMessageListener extends ContrailMessageHandler {

	static final Logger LOGGER = LogManager.getLogger(ProfileMessageListener.class);

	// This used to identify the payload type in the AMQ message
	private static final String CANDE_PAYLOAD_TYPE = "ContentDelivery";

	RecordWriter kinesisRecordWriter;
	// used to generate JSON payload
	Map<String, String> payloadMap = new HashMap<String, String>();

	/**
	 * @param kinesisRecordWriter
	 */
	public ProfileMessageListener(RecordWriter kinesisRecordWriter) {
		this.kinesisRecordWriter = kinesisRecordWriter;
	}

	/**
	 * This method is called by the PCI framework whenever there is a message
	 * received from AMQ
	 */

	public void processMessage(ICommit commitHandle) throws ContrailException {
		LOGGER.info("ProfileMessageListener-processMessage: Rreceived message ");
		long start = Instant.now().toEpochMilli();

		ContrailMessage contrailMessage = commitHandle.getContrailMessage();

		LOGGER.info("MSG Received - Payload: " + Strings.nullToEmpty(contrailMessage.getPayloadType()) + " msgname:"
				+ contrailMessage.getMessageName());
		try {
			String payload = parsePayloadToJSON(contrailMessage);

			// covert the XML payload to JSON

			PutRecordResult putRecordResult = kinesisRecordWriter.putRecord(payload.getBytes());
			LOGGER.info("Message successfully written to kinesis stream with seqnuence no:"
					+ putRecordResult.getSequenceNumber());
			LOGGER.info("Committing results for " + payloadMap.get("fileName"));
			commitHandle.commit();
			long end = Instant.now().toEpochMilli() - start;
			LOGGER.info("Time taken(ms)----" + end);
		} catch (XMLStreamException xmlse) {
			handleError("Error parsing payload: " + contrailMessage.getPayloadData(), xmlse, commitHandle);

		} catch (JsonProcessingException jsonExp) {
			handleError("Error converting the payload to JSON:", jsonExp, commitHandle);
		} catch (ProvisionedThroughputExceededException ptee) {
			handleError("Provisioned Throughput Exceeded for kinesisstream:", ptee, commitHandle);
			// TODO: DO we need back-off algorithm? I am assuming PCI framework should
			// already be doing this?

		} catch (Throwable exp) {

			handleError("Exception occured when trying to stream the AMQ payload to the KinesisDataStream:", exp,
					commitHandle);
		}

	}

	/**
	 * This method parses the xML payload and converts relevant fields into JSON.
	 * 
	 * @param message
	 *            Message received from AMQ(PCI)
	 * @return JSON string payload
	 * @throws XMLStreamException
	 * @throws JsonProcessingException
	 * 
	 */
	public String parsePayloadToJSON(ContrailMessage message) throws XMLStreamException, JsonProcessingException {
		LOGGER.info("Parsing payload");
		if (CANDE_PAYLOAD_TYPE.equals(message.getPayloadType())) {
			// name of the file
			XMLString fileName = new XMLString();
			// Object URL in contentdelivery bucket
			XMLString s3Uri = new XMLString();
			// productId=LIVE to identify live feed or migration
			XMLString productId = new XMLString();

			new XMLParser().getInnerTextFor("/ContentDeliveryData/fileTickets/fileTicket/fileName", fileName::set)
					.getInnerTextFor("/ContentDeliveryData/fileTickets/fileTicket/s3Uri", s3Uri::set)
					.getInnerTextFor("/ContentDeliveryData/fileTickets/fileTicket/directory", productId::set)
					.parse((String) message.getPayloadData());

			this.payloadMap.put("fileName", fileName.get());
			this.payloadMap.put("s3Uri", s3Uri.get());
			this.payloadMap.put("productId", productId.get());

			String transformedPayload = new ObjectMapper().writeValueAsString(this.payloadMap);
			LOGGER.info("Parsed payload: " + transformedPayload);
			// converts the map to json string
			return transformedPayload;

		}

		else {
			throw new IllegalStateException(
					"Unidentified Message Payload: " + Strings.nullToEmpty(message.getPayloadType()));
		}

	}

	/**
	 * simple method to handle errors
	 * 
	 * @param message
	 * @param t
	 * @param commitHandle
	 */
	protected void handleError(String message, Throwable t, ICommit commitHandle) {
		LOGGER.error(message, t);
		LOGGER.info("rollingback commit");
		commitHandle.rollback();

	}

}
