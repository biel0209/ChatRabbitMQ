package br.ufs.dcomp.ExemploRabbitMQ;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class Chat {

    static String user;
    static String receiver;
    static String view = " ";
    final static String ip_aws = "100.26.224.49";
    public static void main(String[] argv) throws Exception {
        Scanner sc = new Scanner(System.in);
        Emissor emissor = new Emissor();
        Receptor receptor = new Receptor();
        Serializador serializador = new Serializador();

        System.out.print("User: ");
        user = sc.nextLine();

        emissor.createQueue(user);

        receptor.receive(user);

        String message = "";

        System.out.print(">> ");

        while(!message.equals("exit")){
            message = sc.nextLine();

            if(message.substring(0, 1).equals("@")){
                receiver = message.substring(1, message.length());
                view = "@" + receiver;
            }
            else if(message.substring(0, 1).equals("!")){
                String[] arguments  = message.split(" ");
                String exChange;
                switch(arguments[0]){
                    case "!addGroup":
                        exChange = arguments[1];
                        emissor.createExchange(user, exChange);
                        break;
                    case "!addUser":
                        String newUser = arguments[1];
                        exChange = arguments[2];
                        emissor.addUser(newUser, exChange);
                        break;
                    case "!delFromGroup":
                        String deleteUser = arguments[1];
                        exChange = arguments[2];
                        emissor.removeUser(deleteUser, exChange);
                        break;
                    case "!removeGroup":
                        exChange = arguments[1];
                        emissor.deleteExchange(exChange);
                        break;
                }
            }
            else if (message.substring(0, 1).equals("#")) {
                String group = message.substring(1, message.length());
                view = "#" + group;
                receiver = group;
            }
            else if (view.substring(0, 1).equals("#")){
                byte[] msg = serializador.serialize(message, user, receiver);
                emissor.multipleSend(receiver, msg);
            }
            else if (message.equals("exit")){
                break;
            }
            else{
                //string vazia representa que a mensagem não é pra um grupo
                byte[] msg = serializador.serialize(message, user, "");
                emissor.simpleSend(receiver, msg);
            }

            System.out.print(view + ">> ");
        }
//        sc.close();
    }
}