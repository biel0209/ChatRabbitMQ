# ChatRabbitMQ
    
Compilar: mvn clean compile assembly:single

Projeto de criação de um chat de linha de comando com funcionalidades como troca de mensagens e arquivos entre pessoas e grupos.

Projeto final da disciplina de Sistemas Distribuídos de 2022.2.

## Funcionalidades do chat

### Envio de mensagens

@usuario -> setar o usuário de destino com quem será feito a troca de mensagens.

#grupo -> setar o grupo de destino com quem será feito a troca de mensagens.

### Gerenciamento de grupos
!addGroup amigos -> criar um grupo chamado amigos.

!addUser joaosantos amigos -> adicionar o usuário joaosantos ao grupo amigos

!delFromGroup joaosantos amigos -> deletar joaosantos do grupo amigos

!removeGroup amigos -> deletar o grupo amigos

### Envio de arquivos

!upload /home/tarcisio/aula1.pdf -> realizar envio do arquivo aula1.pdf para o destinário setado atualmente.
