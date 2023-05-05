package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;


public class Chat {

    static String user;
    static String receiver;
    static String view = "";

    final static String ip_aws = "RabbitMQ-SD-LB-Network-5db364f9d99a904a.elb.us-east-1.amazonaws.com";
    public static void main(String[] argv) throws Exception {
        Scanner sc = new Scanner(System.in);
        Emissor emissor = new Emissor();
        Receptor receptor = new Receptor();
        Serializador serializador = new Serializador();

        System.out.print("User: ");
        user = sc.nextLine();

        emissor.createQueue(user);

        receptor.receive(user);
        receptor.receive(user + "_file");

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
                    case "!upload":
                        String filePath = arguments[1];
                        if (view.substring(0, 1).equals("@")){  // Upload de arquivo para uma única pessoa
                            UploaderArquivo uploader = new UploaderArquivo(filePath, user, "", receiver, false);
                            uploader.start();
                        }else if (view.substring(0, 1).equals("#")){  // Upload de arquivo para um grupo
                            String group = view.substring(view.indexOf("#") + 1);
                            UploaderArquivo uploader = new UploaderArquivo(filePath, user, group, group, true);
                            uploader.start();
                        }
                        break;
                    case "!listUsers":
                        exChange = arguments[1];
                        emissor.listUsers(exChange);
                        break;
                    case "!listGroups":
                        emissor.listGroups(user);
                        break;

                }
            }
            else if (message.substring(0, 1).equals("#")) {
                String group = message.substring(1, message.length());
                view = "#" + group;
                receiver = group;
            }
            else if (view.substring(0, 1).equals("#")){
                byte[] msg = serializador.serialize(message, user, receiver, false);
                emissor.multipleSend(receiver, msg, false);
            }
            else if (message.equals("exit")){
                break;
            }
            else{
                //string vazia representa que a mensagem não é pra um grupo
                byte[] msg = serializador.serialize(message, user, "", false);
                emissor.simpleSend(receiver, msg);
            }
            System.out.print(view + ">> ");
        }
//        sc.close();
    }
}