

package cl.inria.massda;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;

import java.io.IOException;



class Consumer{

	Connection connection;
	Channel channel;
	String exchangeName;
	String queueName;
	String routingKey;

	public Consumer(String hostName, String virtualHost, String userId, String password)  throws IOException  {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(userId);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(hostName);
		factory.setPort(5672);
		connection = factory.newConnection();
		channel = connection.createChannel();
		exchangeName = null;
		queueName = null;
		routingKey = null;
		//consumerTag = "";
	}

	public void close() throws IOException {
		channel.close();
		connection.close();
	}

	public void declareExchange(String exchangeName) throws IOException {
		this.exchangeName = exchangeName;
		channel.exchangeDeclare(exchangeName, "direct", true);
	}

	public void declareQueue(String queueName, String routingKey) throws IOException {
		this.queueName = queueName;
		this.routingKey = routingKey;
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);

	}

	public Channel getChannel(){
		return channel;
	}

	public void startConsuming(String consumerTag, DefaultConsumer consumer) throws IOException {
		boolean autoAck = false;
		channel.basicQos(1); // prefetch = 1
		channel.basicConsume(queueName, autoAck, consumerTag, consumer);
	}

	public void stopConsuming(String consumerTag) throws IOException {
		channel.basicCancel(consumerTag);
	}


}