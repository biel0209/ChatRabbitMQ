package br.ufs.dcomp.ChatRabbitMQ;

public class Desserializador
{
    public void deserialize() throws Exception
    {
        //recebimento de um vetor de bytes
        byte[] buffer = new byte[1000];

        // Mapeando bytes para a mensagem protobuf
        MensagemOuterClass.Mensagem mensagem = MensagemOuterClass.Mensagem.parseFrom(buffer);

        // Extraindo dados da mensagem
        String emissor = mensagem.getEmissor();
        String date = mensagem.getData();
        String hora = mensagem.getHora();
        String group = mensagem.getGrupo();
        MensagemOuterClass.Conteudo conteudo = mensagem.getConteudo();
        String tipoConteudo = conteudo.getTipo();
        String corpoConteudo = conteudo.getCorpo().toStringUtf8();
        String nomeConteudo = conteudo.getNome();
    }
}
