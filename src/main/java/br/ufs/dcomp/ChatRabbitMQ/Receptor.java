package br.ufs.dcomp.ChatRabbitMQ;

import com.google.protobuf.ByteString;
import com.rabbitmq.client.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class Receptor {
  String filePathDestino = "";  // String para definir onde o arquivo consumido será salvo. Depende do SO utilizado.
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

        if (tipoConteudo.equals("text/plain")){
          String corpoConteudo = conteudo.getCorpo().toStringUtf8();
          String nomeConteudo = conteudo.getNome();
          Chat chat = new Chat();
          if(group.equals("")){      //se group é uma string vazia, a mensagem foi enviada apenas para uma pessoa
            System.out.println("\n" + "(" + date + " às " + hora + ") " + emissor + " diz: " + corpoConteudo);
            System.out.print(chat.view + ">> ");
          }else if(!group.equals("") && !emissor.equals(user)){
            System.out.println("\n" + "(" + date + " às " + hora + ") " + emissor + "#" + group + " diz: " + corpoConteudo);
            System.out.print(chat.view + ">> ");
          }
        }
        else{
          ByteString corpoConteudo = conteudo.getCorpo();
          String nomeConteudo = conteudo.getNome();
          String sistemaOperacional = System.getProperty("os.name");
          String usuarioSO = System.getProperty("user.name");
          if (sistemaOperacional.contains("Windows")){
            filePathDestino = "C:/Users/" + usuarioSO + "/Downloads/" + nomeConteudo;
          }else if (sistemaOperacional.contains("Linux")){{
            filePathDestino = "/home/" + usuarioSO + "/" + nomeConteudo;
          }}
          File arquivo = new File(filePathDestino);
          FileOutputStream in = new FileOutputStream(arquivo);
          in.write(corpoConteudo.toByteArray());
          in.flush();
          in.close();
          System.out.println("\n" + "(" + date + " às " + hora + ") Arquivo \"" + nomeConteudo + "\" recebido de @" + emissor + ".");
        }


//        (deliveryTag,               multiple);
//        channel.basicAck(envelope.getDeliveryTag(), false);    //confirmar o recebimento ao rabbitmq
      }
    };
    //(queue-name, autoAck, consumer);
    channel.basicConsume(QUEUE_NAME, true,    consumer);
  }
}




