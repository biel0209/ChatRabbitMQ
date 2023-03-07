package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


public class Emissor {

  public ConnectionFactory createFactory() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    Chat chat = new Chat();
    factory.setHost(chat.ip_aws); // Alterar          ip-da-instancia-da-aws
    factory.setUsername("admin"); // Alterar     usuário-do-rabbitmq-server
    factory.setPassword("password"); // Alterar            senha-do-rabbitmq-server
    factory.setVirtualHost("/");
    return factory;
  }
  public void createQueue(String user) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String QUEUE_NAME = user;

    //(queue-name, durable, exclusive, auto-delete, params);
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);

    channel.close();
    connection.close();
  }

  public void createExchange(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

    String queueName = user;

    channel.queueBind(queueName, EXCHANGE_NAME, "");

    channel.close();
    connection.close();
  }

  public void deleteExchange(String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDelete(EXCHANGE_NAME);

    channel.close();
    connection.close();
  }


  public void addUser(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String queueName = user;

    channel.queueBind(queueName, EXCHANGE_NAME, "");

    channel.close();
    connection.close();
  }

  public void removeUser(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String queueName = user;

    channel.queueUnbind(queueName, EXCHANGE_NAME, "");

    channel.close();
    connection.close();
  }

  public void simpleSend(String userReceiver, byte[] message) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String QUEUE_NAME = userReceiver;

    //(queue-name, durable, exclusive, auto-delete, params);
    channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null);  //caso o usuário de destino não exista

    //  (exchange, routingKey, props, message-body             );
    channel.basicPublish("",       userReceiver, null,  message);

    channel.close();
    connection.close();
  }

  public void multipleSend(String receiver, byte[] message) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String EXCHANGE_NAME = receiver;

    //(queue-name, durable, exclusive, auto-delete, params);
    channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

    //  (exchange, routingKey, props, message-body             );
    channel.basicPublish(EXCHANGE_NAME, "", null, message);

    channel.close();
    connection.close();
  }
}