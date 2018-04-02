
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

public class JConsumer implements ExceptionListener, MessageListener {
	public static void main(String[] args) throws IOException {
		 JConsumer consumer = new JConsumer();
		 System.out.println("here 1");
		 consumer.consumeMessage("publishsubscribe.t");

//		ClassLoader classLoader = JConsumer.class.getClassLoader();
//		File file = new File(classLoader.getResource("testout.txt").getFile());
//		System.out.println("file.getAbsolutePath() "+file.getCanonicalPath());
//		
//		FileInputStream fin = new FileInputStream("/testout.txt");
//		int i = 0;
//		while ((i = fin.read()) != -1) {
//			System.out.print((char) i);
//		}

	}

	public void consumeMessage(String queueName) {
		try {
			System.out.println("here 2");
			String brokerURL = System.getenv("broker.url");
			System.out.println("Connecting to broker : "+brokerURL);

			// create a Connection Factory
			// ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin",
			// "H4uqiyv3FdC2",
			// "tcp://34.238.233.213:61616");
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin",
					brokerURL);
			System.out.println("Connected to broker : "+brokerURL);
			Connection connection = connectionFactory.createConnection();
			connection.setClientID("profileamq_client");
			connection.start();
			connection.setExceptionListener(this);
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic destination = session.createTopic(queueName);
			MessageConsumer consumer = session.createDurableSubscriber(destination, "publishsubscribe");

			// ASync
			consumer.setMessageListener(this);
			// Sync
			/*
			 * Message message = consumer.receive(1000); TextMessage textMessage =
			 * (TextMessage)message; String text = textMessage.getText();
			 * System.out.println("Received : " + text);
			 */
			//
			// consumer.close();
			// session.close();
			// connection.close();
		} catch (Exception e) {
			System.out.println("Caught: " + e);
			e.printStackTrace();
		}

	}

	public void onException(JMSException arg0) {
		System.out.println("JMS Exception occured.  Shutting down client.");
	}

	public void onMessage(Message message) {
		try {
			TextMessage textMessage = (TextMessage) message;
			String text = textMessage.getText();
			System.out.println("Received: " + text);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}

