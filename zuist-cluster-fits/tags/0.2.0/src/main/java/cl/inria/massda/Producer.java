

package cl.inria.massda;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;


class Producer{

	String exchangeName;
	Connection connection;
	Channel channel;

	public Producer(String exchangeName, String hostName, String virtualHost, String userId, String password)  throws IOException  {
		this.exchangeName = exchangeName;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(userId);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(5672);
		connection = factory.newConnection();
		channel = connection.createChannel();

	}

	public void publish(String message, String routingKey) throws IOException {
		channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
		System.out.println(" [x] sent " + message);
	}

	public void close() throws IOException {
		channel.close();
		connection.close();
	}


}