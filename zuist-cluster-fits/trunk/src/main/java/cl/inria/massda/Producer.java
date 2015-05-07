

package cl.inria.massda;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;


class Producer{

	String exchangeName;
	Connection connection;
	Channel channel;
	String queueName;
	String routingKey;

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
		channel.exchangeDeclare(exchangeName, "direct", true);

	}

	public void declareQueue(String queueName, String routingKey) throws IOException {
		this.queueName = queueName;
		this.routingKey = routingKey;
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);

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