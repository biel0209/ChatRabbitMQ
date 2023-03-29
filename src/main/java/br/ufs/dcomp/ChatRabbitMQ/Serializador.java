package br.ufs.dcomp.ChatRabbitMQ;

import com.google.protobuf.ByteString;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Serializador
{
    public byte[] serialize(String mensagemFonte, String emissor, String group, boolean isFile) throws Exception {

        //Capturando data e hora
        ZoneId saoPauloZoneId = ZoneId.of("America/Sao_Paulo");
        LocalDateTime now = LocalDateTime.now(saoPauloZoneId);
        String hour = now.getHour() + ":" + now.getMinute() + ":" + now.getSecond();
        String date = now.toLocalDate().toString();

        if (isFile){
            // Obtendo a representação em ByteString do arquivo
            String filePath = mensagemFonte;
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                System.out.println("Arquivo não encontrado: " + filePath);
            }
            byte[] arquivoEmBytes = Files.readAllBytes(path);
            ByteString conteudoArquivo = ByteString.copyFrom(arquivoEmBytes);


            // Agrupando dados do conteudo da mensagem
            MensagemOuterClass.Conteudo.Builder conteudo = MensagemOuterClass.Conteudo.newBuilder();
            Path source = Paths.get(filePath);
            String tipoMime = Files.probeContentType(source);
            conteudo.setTipo(tipoMime); // Tipo do conteúdo no padrão de tipos MIME. Exemplos: "text/plain", "image/png"
            conteudo.setCorpo(conteudoArquivo); // Sequência de bytes que compõe o corpo da mensagem - tipo bytes
            String nomeArquivo = filePath.substring(filePath.lastIndexOf("/") + 1);  // Pegar o nome do arquivo
            conteudo.setNome(nomeArquivo); // Nome do conteúdo, se existente. Exemplos: "logo_ufs.png", "index.html"

            // Agrupando dados da mensagem com o conteudo acima
            MensagemOuterClass.Mensagem.Builder builderMensagem = MensagemOuterClass.Mensagem.newBuilder();
            builderMensagem.setEmissor(emissor);  // Nome do emissor
            builderMensagem.setData(date);  // Data de envio
            builderMensagem.setHora(hour);  // Hora de envio
            builderMensagem.setGrupo(group);  // nome do grupo, se houver
            builderMensagem.setConteudo(conteudo); // Informa o nome do grupo, se a mensagem for para um grupo

            // Obtendo a mensagem
            MensagemOuterClass.Mensagem mensagem = builderMensagem.build();

            // Serializando a mensagem
            byte[] bufferMsg = mensagem.toByteArray();

            return bufferMsg;
        }

        // Obtendo a representação em bytes da mensagem
        ByteString msg = ByteString.copyFromUtf8(mensagemFonte);

        // Agrupando dados do conteudo da mensagem
        MensagemOuterClass.Conteudo.Builder conteudo = MensagemOuterClass.Conteudo.newBuilder();
        conteudo.setTipo("text/plain"); // Tipo do conteúdo no padrão de tipos MIME. Exemplos: "text/plain", "image/png"
        conteudo.setCorpo(msg); // Sequência de bytes que compõe o corpo da mensagem - tipo bytes
        conteudo.setNome(""); // Nome do conteúdo, se existente. Exemplos: "logo_ufs.png", "index.html"

        // Agrupando dados da mensagem com o conteudo acima
        MensagemOuterClass.Mensagem.Builder builderMensagem = MensagemOuterClass.Mensagem.newBuilder();
        builderMensagem.setEmissor(emissor);  // Nome do emissor
        builderMensagem.setData(date);  // Data de envio
        builderMensagem.setHora(hour);  // Hora de envio
        builderMensagem.setGrupo(group);  // nome do grupo, se houver
        builderMensagem.setConteudo(conteudo); // Informa o nome do grupo, se a mensagem for para um grupo

        // Obtendo a mensagem
        MensagemOuterClass.Mensagem mensagem = builderMensagem.build();

        // Serializando a mensagem
        byte[] bufferMsg = mensagem.toByteArray();

        return bufferMsg;
    }
}
