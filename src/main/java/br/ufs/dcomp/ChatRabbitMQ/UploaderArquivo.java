package br.ufs.dcomp.ChatRabbitMQ;


public class UploaderArquivo extends Thread{
    private String filePath;
    private String userEmissor;
    private String group;
    private String receiver;
    private boolean forGroup;  // Indica se o arquivo será enviado para um grupo

    public UploaderArquivo(String filePath, String userEmissor, String group, String receiver, boolean forGroup) {
        this.filePath = filePath;
        this.userEmissor = userEmissor;
        this.group = group;
        this.receiver = receiver;
        this.forGroup = forGroup;
    }

    public void run(){
        Emissor emissor = new Emissor();
        Serializador serializador = new Serializador();
        Chat chat = new Chat();
        if (forGroup){
            System.out.println("\nEnviando " + filePath + " para o grupo #" + group + ".");
            System.out.print(chat.view + ">> ");
            try{
                byte[] arquivo = serializador.serialize(filePath, userEmissor, group, true);
                emissor.multipleSend(group, arquivo, true);
                System.out.println("\nArquivo " + filePath + " foi enviado para o grupo #" + receiver + ".");
                System.out.print(chat.view + ">> ");
            }catch (Exception e){
                System.out.println("\nErro ao tentar enviar o arquivo");
            }
        }else{
            System.out.println("\nEnviando " + filePath + " para @" + receiver + ".");
            System.out.print(chat.view + ">> ");
            try{
                byte[] arquivo = serializador.serialize(filePath, userEmissor, "", true);
                emissor.simpleSend(receiver + "_file", arquivo);
                System.out.println("\nArquivo " + filePath + " foi enviado para @" + receiver + ".");
                System.out.print(chat.view + ">> ");
            }catch (Exception e){
                System.out.println("\nErro ao tentar enviar o arquivo");
            }
        }
    }
}
