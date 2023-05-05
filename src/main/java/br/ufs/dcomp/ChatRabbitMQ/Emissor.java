package br.ufs.dcomp.ChatRabbitMQ;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

  public void listUsers(String EXCHANGE_NAME) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection1 = factory.newConnection();
    Channel channel = connection1.createChannel();

    String host = "RabbitMQ-SD-LB-Application-1892710110.us-east-1.elb.amazonaws.com";
    String vhost = "%2f";
    String exchangeName = EXCHANGE_NAME;

    ObjectMapper objectMapper = new ObjectMapper();
    String baseUrl = String.format("http://%s/api/exchanges/%s/%s/bindings/source",
            host, vhost, exchangeName);
    URL url = new URL(baseUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(true);
    connection.setRequestProperty("Authorization", "Basic " +
            java.util.Base64.getEncoder().encodeToString("admin:password".getBytes()));
    connection.connect();
    InputStream inputStream = connection.getInputStream();

    JsonNode bindings = objectMapper.readTree(inputStream);
    String allUsers = "";
    for (JsonNode binding : bindings) {
      String queueName = binding.get("destination").asText();
      if(!queueName.endsWith("_file"))
        allUsers += queueName + ", ";
    }
    if(allUsers.length() <= 2)
    {
      System.out.println("Este grupo não possui usuários!");
    }else{
      System.out.println(allUsers.substring(2, allUsers.length() - 2));
    }
    channel.close();
    connection1.close();
  }

  public void listGroups(String user) throws Exception {
    ConnectionFactory factory = createFactory();
    Connection connection1 = factory.newConnection();
    Channel channel = connection1.createChannel();

    String host = "RabbitMQ-SD-LB-Application-1892710110.us-east-1.elb.amazonaws.com";
    String vhost = "%2f";

    ObjectMapper objectMapper = new ObjectMapper();
    String baseUrl = String.format("http://%s/api/queues/%s/%s/bindings",
            host, vhost, user);

    URL url = new URL(baseUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(true);
    connection.setRequestProperty("Authorization", "Basic " +
            java.util.Base64.getEncoder().encodeToString("admin:password".getBytes()));
    connection.connect();
    InputStream inputStream = connection.getInputStream();

    JsonNode bindings = objectMapper.readTree(inputStream);
    String allGroups = "";
    for (JsonNode binding : bindings) {
      String queueName = binding.get("source").asText();
      if(!queueName.endsWith("_file"))
        allGroups += queueName + ", ";
    }
    if(allGroups.length() <= 2)
    {
      System.out.println("Você não esta em nenhum grupo!");
    }else{
      System.out.println(allGroups.substring(2, allGroups.length() - 2));
    }
    channel.close();
    connection1.close();
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