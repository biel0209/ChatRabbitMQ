package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;


public class Chat {

    static String user;
    static String receiver;
    static String view = "";

    final static String ip_aws = "54.89.75.155";
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
                            byte[] arquivo = serializador.serialize(filePath, user, "", true);
                            System.out.println("Enviando " + filePath + " para @" + receiver + ".");
                            emissor.simpleSend(receiver + "_file", arquivo);
                            System.out.println("Arquivo " + filePath + " foi enviado para @" + receiver + ".");
                        }else if (view.substring(0, 1).equals("#")){  // Upload de arquivo para um grupo
                            String group = view.substring(view.indexOf("#") + 1);
                            byte[] arquivo = serializador.serialize(filePath, user, group, true);
                            System.out.println("Enviando " + filePath + " para o grupo #" + group + ".");
                            emissor.multipleSend(group, arquivo, true);
                            System.out.println("Arquivo " + filePath + " foi enviado para o grupo #" + receiver + ".");
                        }
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