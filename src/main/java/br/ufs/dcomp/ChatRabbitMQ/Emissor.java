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

    channel.queueDeclare(QUEUE_NAME + "_file", false,   false,     false,       null);

    channel.close();
    connection.close();
  }

  public void createExchange(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "direct");

    String queueName = user;

    channel.queueBind(queueName, EXCHANGE_NAME, "text");
    channel.queueBind(queueName + "_file", EXCHANGE_NAME, "file");

    channel.close();
    connection.close();
  }

  public void deleteExchange(String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    try{
      channel.exchangeDelete(EXCHANGE_NAME);
    }catch (Exception e){
      System.out.println("Não foi possível remover o grupo. Certifique-se de que " +
              "ele existe.");
    }


    channel.close();
    connection.close();
  }


  public void addUser(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String queueName = user;

    //caso o usuário de destino não exista
    channel.queueDeclare(queueName, false,   false,     false,       null);

    channel.queueBind(queueName, EXCHANGE_NAME, "text");
    channel.queueBind(queueName + "_file", EXCHANGE_NAME, "file");

    channel.close();
    connection.close();
  }

  public void removeUser(String user, String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String queueName = user;

    try{
      channel.queueUnbind(queueName, EXCHANGE_NAME, "text");
      channel.queueUnbind(queueName + "_file", EXCHANGE_NAME, "file");
    }catch (Exception e){
      System.out.println("Não foi possível remover esse usuário. Certifique-se de que" +
              " ele existe.");
    }

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
    channel.basicPublish("",       QUEUE_NAME, null,  message);

    channel.close();
    connection.close();
  }

  public void multipleSend(String receiver, byte[] message, boolean isFile) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String EXCHANGE_NAME = receiver;
    String routingKey = "text";

    if(isFile){
      routingKey = "file";
    }

    try{
      //  (exchange, routingKey, props, message-body             );
      channel.basicPublish(EXCHANGE_NAME, routingKey, null, message);
    }catch (Exception e){
      System.out.println("Não foi possível enviar uma mensagem para o grupo. Certifique-se de que" +
              " ele existe.");
    }

    channel.close();
    connection.close();
  }
}