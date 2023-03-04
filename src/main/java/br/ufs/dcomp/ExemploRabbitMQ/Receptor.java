package br.ufs.dcomp.ExemploRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Receptor {
  public void receive(String user) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    Chat chat = new Chat();
    factory.setHost(chat.ip_aws); // Alterar          ip-da-instancia-da-aws
    factory.setUsername("admin"); // Alterar     usuário-do-rabbitmq-server
    factory.setPassword("password"); // Alterar            senha-do-rabbitmq-server
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    String QUEUE_NAME = user;

    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
        MensagemOuterClass.Mensagem message = MensagemOuterClass.Mensagem.parseFrom(body);
        String emissor = message.getEmissor();
        String date = message.getData();
        String hora = message.getHora();
        String group = message.getGrupo();
        MensagemOuterClass.Conteudo conteudo = message.getConteudo();
        String tipoConteudo = conteudo.getTipo();
        String corpoConteudo = conteudo.getCorpo().toStringUtf8();
        String nomeConteudo = conteudo.getNome();

        if(group.equals("")){      //se group é uma string vazia, a mensagem foi enviada apenas para uma pessoa
          System.out.println("\n" + "(" + date + " às " + hora + ") " + emissor + " diz: " + corpoConteudo);
        }else{
          System.out.println("\n" + "(" + date + " às " + hora + ") " + emissor + "#" + group + " diz: " + corpoConteudo);
        }

        Chat chat = new Chat();
        System.out.print(chat.view + ">> ");

//        (deliveryTag,               multiple);
        channel.basicAck(envelope.getDeliveryTag(), false);    //confirmar o recebimento ao rabbitmq
      }
    };
    //(queue-name, autoAck, consumer);
    channel.basicConsume(QUEUE_NAME, true,    consumer);
  }
}




