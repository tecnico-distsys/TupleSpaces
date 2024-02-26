
# TupleSpaces

Este documento descreve o projeto da cadeira de Sistemas Distribuídos 2023/2024.

## 1 Introdução


O objetivo do projeto de Sistemas Distribuídos (SD) é desenvolver o sistema **TupleSpace**, um serviço que implementa um *espaço de tuplos* distribuído. 
O sistema será concretizado usando gRPC e Java (com uma exceção, descrita mais à frente neste enunciado).

O serviço permite a um ou mais utilizadores (também designados por _workers_ na literatura) colocarem tuplos no espaço partilhado, lerem os tuplos existentes, assim como retirarem tuplos do espaço. Um tuplo é um conjunto ordenado de campos *<campo_1, campo_2, ..., campo_n>*. 
Neste projeto, um tuplo deve ser instanciado como uma cadeia de carácteres (*string*).
Por exemplo, a *string* contendo `"<vaga,sd,turno1>"`.

No espaço de tuplos podem co-existir várias instâncias idênticas.
Por exemplo, podem existir múltiplos tuplos `"<vaga,sd,turno1>"`, indicando a existência de várias vagas. 

É possível procurar, no espaço de tuplos, por um dado tuplo para ler ou retirar.
Na variante mais simples, pode-se pesquisar por um tuplo concreto. Por exemplo, `"<vaga,sd,turno1>"`.
Alternativamente, é possível usar [expressões regulares Java](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) para permitir o emparelhamento com múltiplos valores. Por exemplo, `"<vaga,sd,[^,]+>"` 
tanto emparelha com  `"<vaga,sd,turno1>"`, com  `"<vaga,sd,turno2>"`.

Mais informação sobre os espaços de tuplos distribuídos, assim como a descrição de um sistema que concretiza esta abstração pode ser encontrada na bibilografia da cadeira e no seguinte artigo:

A. Xu and B. Liskov. [A design for a fault-tolerant, distributed implementation of linda](http://www.ai.mit.edu/projects/aries/papers/programming/linda.pdf). In 1989 The Nineteenth International Symposium on Fault-Tolerant Computing. Digest of Papers(FTCS), pages 199–206.

As operações disponíveis para o utilizador são as seguintes [^1] *put*, *read*, *take* e *getTupleSpacesState*.

[^1]: Usamos a nomenclatura em Inglês da bibliografia da cadeira, mas substituímos o nome *write* por *put*, que nos parece mais claro. Note-se que o artigo original usa uma nomenclatura diferente.

* A operação *put* acrescenta um tuplo ao espaço partilhado.

* A operação *read* aceita a descrição do tuplo (possivelmente com expressão regular) e retorna *um* tuplo que emparelhe com a descrição, caso exista. Esta operação bloqueia o cliente até que exista um tuplo que satisfaça a descrição. O tuplo *não* é retirado do espaço de tuplos.

* A operação *take* aceita a descrição do tuplo (possivelmente com expressão regular) e retorna *um* tuplo que emparelhe com a descrição. Esta operação bloqueia o cliente até que exista um tuplo que satisfaça a descrição. O tuplo *é* retirado do espaço de tuplos.

* A operação *getTupleSpacesState* recebe como único argumento o qualificador do servidor que se pretende consultar e retorna todos tuplos presentes nesse servidor.

Os utilizadores acedem ao serviço **TupleSpace** através de um processo cliente, que interage 
com um ou mais servidores que oferecem o serviço, através de chamadas a procedimentos remotos.

## 2 Variantes do projecto

Serão pedidas três variantes do projecto, que ilustram diferentes formas de concretizar o serviço. Duas variantes são obrigatórios e uma é opcional. Para cada variante, a interface do servidor será ligeiramente diferente, uma vez que os pedidos são processados de forma distinta. Cada uma destas variantes pode ser concretizada por etapas. As variantes são as seguintes:

### Variante R1

Desenvolver uma solução em que o serviço é prestado por um único servidor (ou seja, uma arquitetura cliente-servidor simples, sem replicação de servidores), que aceita pedidos num endereço/porto fixo.
Os clientes devem usar os *blocking stubs* do gRPC.

#### Etapa 1.1

O porto do servidor que é conhecido de antemão por todos os clientes.

#### Etapa 1.2

Os clientes não sabem, quando são lançados, qual o endereço do servidor. Para permitir que o endereço dos servidores seja descoberto dinamicamente, a solução deverá recorrer a um servidor de nomes concretizado em Python (seguindo as instruções no guião "gRPC multi-linguagem").

### Variante R2

Desenvolver uma solução alternativa em que o serviço é replicado, em três servidores (A, B e C), seguindo o algoritmo de Xu e Liskov (citado acima).
Tal como na variante anterior, o cliente deve descobrir o endereço dos servidores dinamicamente através do servidor de nomes.

Nesta solução, o cliente utilizador deve recorrer também a um *non-blocking stub* do gRPC.

Nota: A operação *getTupleSpacesState* não deve ser replicada.

#### Etapa 2.1

Desenvolver as operações _read_ e _put_ (não suportando, para já, a operação _take_).
Quando um cliente pretende invocar uma dessas operações, 
começa por enviar o pedido a todos os servidores e depois aguarda pelas 
respostas (de um servidor, no caso de _read_, ou de todos os servidores, no caso de _put_).


#### Etapa 2.2

Desenvolver também o código necessário para executar a operação _take_ de acordo com o algoritmo de Xu/Liskov (citado acima). Este algoritmo executa a operação de _take_ em dois passos (em algumas situações, o primeiro passo pode ser repetido).
Deve ser adotada a variante síncrona (ou seja, o cliente que a invoca aguarda até ambos os passos terminarem, e só então retorna).

Cada tuplo mantido no servidor deve, além da sua _string_, ter os seguintes campos adicionais: (i) uma _flag_ que indica se o tuplo está trancado por algum 
cliente (que executou o primeiro passo da operação _take_ mas ainda não completou 
o segundo passo), e (ii) um identificador desse cliente.
O identificador do cliente é um número único que este passa junto com os pedidos _take_. (Nota: este aspeto difere parcialmente do artigo original.)

### Variante 3

Desenvolver uma variante baseada na abordagem de replicação de máquina de estados (RME), portanto uma alternativa diferente ao algoritmo de Xu/Liskov. Note-se que tanto a Variante 2 como a Variante 3 possuem vantagens e desvantagens, podendo apresentar um desempenho melhor ou pior consoante o padrão de utilização do espaço de tuplos.

Tal como na variante 2, o serviço deve ser suportado por 3 servidores (réplicas), o cliente deve descobrir o endereço dos servidores dinamicamente através do servidor de nomes, e o cliente utilizador deve recorrer a um *non-blocking stub* do gRPC.

#### Etapa única 3.1

Nesta alternativa, o cliente que pretende invocar uma operação _put_ ou _take_
começa por contactar um serviço remoto que lhe fornece um número de sequência único.
De sequida, o cliente envia finalmente o pedido aos servidores TupleSpace, 
juntamente com o número de sequência.
Os servidores devem  processar os pedidos _put_/_take_ em ordem total,
implementando assim uma máquina de estados replicada. 
Compete a cada grupo definir o algoritmo (executado pelos servidores) que
assegure esse objetivo.
Para tal, devem aproveitar os números de sequência enviados junto com cada pedido.


Nota: a implementação do serviço remoto que fornece os números de sequência é fornecida pelos docentes.


## 3 Faseamento da execução do projeto

Os alunos poderão optar por desenvolver apenas as variantes 1 e 2 do projecto (nível de dificuldade "Bring 'em on!") ou também a variante 3 (nível de dificuldade "I am Death incarnate!"). Note-se que o nível de dificuldade "Don't hurt me" não está disponível neste projecto.

O nível de dificuldade escolhido afeta a forma como o projeto de cada grupo é avaliado e a cotação máxima que pode ser alcançada (ver Secção 6 deste anunciado).

O projeto é executado em 3 fases. A data final de cada fase (ou seja, a data de cada entrega) está publicada no site dos laboratórios de SD. 

Dependendo do nível de dificuldade seguido, o faseamento das etapas ao longo do tempo será distinto.

### Faseamento do nível de dificuldade "Bring 'em on!"

#### Fase 1

  - Etapa 1.1

#### Fase 2

  - Etapas 1.2 e 2.1

#### Fase 3

  - Etapa 2.2

### Faseamento do nível de dificuldade "I am Death incarnate!"

#### Fase 1

  - Etapas 1.1 e 1.2

#### Fase 2

  - Etapas 2.1 e 2.2

#### Fase 3

  - Etapa 3.1



## 4 Processos


### Servidores *TupleSpaces*

Existem até 3 implementações alternativas 
de servidores *TupleSpaces*.
Cada tipo de implementação fornece uma interface remota distinta aos
clientes.
As 3 interfaces remotas encontram-se definidas nos ficheiros *proto* que são fornecidos pelo corpo docente juntamente com este enunciado.

Os servidores devem ser lançados recebendo como argumentos o porto e o seu qualificador ('A', 'B' ou 'C'). Na fase 1, o qualificador passado é ignorado.
Por exemplo, o servidor A pode ser lançada da seguinte forma (**$** representa a *shell* do sistema operativo):

`$ mvn exec:java -Dexec.args="2001 A"`


### Servidor de nomes

O servidor de nomes permite aos servidores registarem o seu endereço para ser conhecido por outros que estejam presentes no sistema.

O servidor de nomes deve ser lançado sem argumentos e ficará à escuta no porto `5001`, podendo ser lançado a partir da pasta `NameServer` da seguinte forma:

`$ python server.py`

O porto 5001 é bem conhecido pelos clientes e servidores.
Um servidor, quando se regista no servidor de nomes, indica o nome do serviço (neste caso *TupleSpace*), o seu endereço e um qualificador, que pode assumir os valores 'A', 'B' ou 'C'. 
Os clientes podem obter o endereço dos servidores, fornecendo o nome do serviço e o qualificador.



### Clientes


Processos cliente recebem comandos a partir da consola. Todos os processos cliente deverão mostrar o símbolo *>* sempre que se encontrarem à espera que um comando seja introduzido.

Para todos os comandos, caso não ocorra nenhum erro, os processos cliente devem imprimir "OK" seguido da mensagem de resposta, tal como gerada pelo método toString() da classe gerada pelo compilador `protoc`, conforme ilustrado nos exemplos abaixo. 

No caso em que um comando origina algum erro do lado do servidor, esse erro deve ser transmitido ao cliente usando os mecanismos do rRPC para tratamento de erros (no caso do Java, encapsulados em exceções). Nessas situações, quando o cliente recebe uma exceção após uma invocação remota, este deve simplesmente imprimir uma mensagem que descreva o erro correspondente.

Enquanto o projeto não incorporar o servidor de nomes, os programas de ambos os tipos de clientes recebem como argumentos o nome da máquina e porto onde o servidor do TupleSpace pode ser encontrado. Por exemplo:

`$ mvn exec:java -Dexec.args="localhost 2001"`

A partir do momento em que há servidor de nomes, os programas cliente deixam de receber os argumentos acima (nome da máquina e porto).

Para a etapa 2.2 (operação _take_ usando o algoritmo de replicação de Xu/Liskov), os programas cliente devem receber como argumento um identificador de cliente
(um inteiro que se pressupõe único entre processos cliente).

Existe um comando para cada operação do serviço: `put`, `read`,  `take` e `getTupleSpacesState`. 
Os 3 primeiros recebem 
Uma *string*, delimitada por `<` e `>` e sem conter qualquer espaço entre esses símbolos, que define um tuplo ou, no caso dos comandos `read` e `take`, uma expressão regular (usando a sintaxe das expressões regulares em Java) que especifica o padrão de tuplos pretendidos.
O `getTupleSpacesState` recebe o qualificador do servidor alvo.

Um exemplo:

```
> put <vaga,sd,turno1>
OK

> put <vaga,sd,turno2>
OK

> take <vaga,sd,turno1>
OK
<vaga,sd,turno1>

> read <vaga,sd,[^,]+>
OK
<vaga,sd,turno2>
```


Existem também três comandos adicionais, que não resultam em invocações remotas: 

- `setdelay`, que recebe como argumentos o identificador de um servidor e um inteiro. Daí em diante, sempre que o cliente se prepare para enviar pedidos a esse servidor, 
o cliente deve primeiro esperar o número de segundos que foi definido no 2º argumento e só depois envia o pedido ao servidor. 
O comando `setdelay` pode ser chamado múltiplas vezes, permitindo assim redefinir ou mesmo anular o atraso associado a cada servidor.

-  `sleep`, que bloqueia o cliente pelo número de segundos passado como único argumento.

-  `exit`, que termina o cliente.

Os comandos `setdelay` e `sleep` poderão ser úteis para depurar o comportamento dos algoritmos distribuídos
construídos nas diferentes etapas do projeto, especialmente nas mais avançadas.




## 5 Outras considerações

### Tecnologias

Todos os componentes do projeto têm de ser implementados na linguagem de
programação [Java](https://docs.oracle.com/javase/specs/) (com a exceção do servidor de nomes, em Python).
A ferramenta de construção a usar, obrigatoriamente, é o [Maven](https://maven.apache.org/).

A invocação remota de serviços deve ser suportada por serviços [gRPC](https://grpc.io/). Os serviços implementados devem obedecer aos *protocol buffers* fornecidos no código base disponível no repositório github do projeto.

### Opção de *debug*

Todos os processos devem poder ser lançados com uma opção "-debug". Se esta opção for seleccionada, o processo deve imprimir para o "stderr" mensagens que descrevam as ações que executa. O formato destas mensagens é livre, mas deve ajudar a depurar o código. Deve também ser pensado para ajudar a perceber o fluxo das execuções durante a discussão final.


### Modelo de Interação, Faltas e Segurança


Deve assumir-se que nem os servidores nem os clientes podem falhar. 
Deve também assumir-se que as ligações TCP (usadas pelo gRPC) tratam situações de perda, reordenação ou duplicação de mensagens.  
No entanto, as mensagens podem atrasar-se arbitrariamente, logo o sistema é assíncrono. 

Fica fora do âmbito do projeto resolver os problemas relacionados com a segurança (e.g., autenticação dos 
utilizadores, confidencialidade ou integridade das mensagens).


### Persistência

Não se exige nem será valorizado o armazenamento persistente do estado dos servidores.

### Validações

Os argumentos das operações devem ser validados obrigatoriamente e de forma estrita pelo servidor.


# 5 Resumo

Em resumo, é necessário implementar:

- Na fase 1 (1ª entrega):
  
  - *Obrigatório* ("Bring 'em on!" mode)
    - servidor único;
    - clientes;
      
  - *Opcional* ("I am Death incarnate!" mode)
    - servidor de nomes e cliente agora a consultar o servidor de nomes

- Na fase 2 (2ª entrega):
  
  - *Obrigatório* ("Bring 'em on!" mode)
    - servidor de nomes
    - clientes, agora a consultar o servidor de nomes.
    - replicação de _read_ e _put_ síncrono
      
  - *Opcional* ("I am Death incarnate!" mode)
    -  replicação de _take_ Xu & Liskov síncrono 

  Na fase 3 (3ª entrega):

  - *Obrigatório* ("Bring 'em on!" mode)
    - replicação de _take_ Xu & Liskov síncrono 
      
  - *Opcional* ("I am Death incarnate!" mode)
    - replicação de _put_ e _take_ com ordem total

  

## 6 Avaliação


A avaliação segue as seguintes regras:

- Na primeira entrega, o grupo indica (no documento `README.md` na pasta raíz do seu projeto) qual o nível de dificuldade em que o grupo decidiu trabalhar. 

- Nas entregas seguintes, caso pretenda, o grupo pode baixar o nível de dificuldade (ou seja, passar de "I am Death incarnate!" para "Bring 'em on!"). 
Um grupo que esteja no nível "Bring 'em on!" em qualquer fase do projeto já não poderá subir de nível nas entregas seguintes.

- No final de cada fase, o corpo docente avaliará o conjunto de etapas que são exigidas nessa fase para o nível de dificuldade escolhido 
pelo grupo nesse momento (ver Secção 3).
A classificação obtida pelo grupo nessa fase depende da completude e qualidade do código relevante para esse conjunto de etapas.

- Uma vez avaliada uma etapa no final de uma dada fase, essa etapa já não será reavaliada nas fases seguintes. Por exemplo, se um grupo opta por "I am Death incarnate!" mas submete uma solução incompleta da etapa 1.2 no final da 1ª fase (obtendo uma cotação baixa), 
os pontos perdidos na cotação dessa etapa já não serão recuperados em fases seguintes (mesmo que o grupo complete ou melhore o 
código correspondente, ou mesmo que o grupo baixe de nível de dificuldade).


### Cotações das etapas

A cotação máxima de cada etapa é a seguinte:

- Etapa 1.1: 6 valores (30%)
- Etapa 1.2: 2 valores (10%) 
- Etapa 2.1: 5 valores (25%)
- Etapa 2.2: 5 valores (25%)
- Etapa 3:   2 valores (10%)

### Fotos

Cada membro da equipa tem que atualizar o Fénix com uma foto, com qualidade, tirada nos últimos 2 anos, para facilitar a
identificação e comunicação.

### Identificador de grupo

O identificador do grupo tem o formato `GXX`, onde `G` representa o campus e `XX` representa o número do grupo de SD
atribuído pelo Fénix. Por exemplo, o grupo A22 corresponde ao grupo 22 sediado no campus Alameda; já o grupo T07
corresponde ao grupo 7 sediado no Taguspark.

O grupo deve identificar-se no documento `README.md` na pasta raiz do projeto.

Em todos os ficheiros de configuração `pom.xml` e de código-fonte, devem substituir `GXX` pelo identificador de grupo.

Esta alteração é importante para a gestão de dependências, para garantir que os programas de cada grupo utilizam sempre
os módulos desenvolvidos pelo próprio grupo.

### Colaboração

O [Git](https://git-scm.com/doc) é um sistema de controlo de versões do código fonte que é uma grande ajuda para o
trabalho em equipa.

Toda a partilha de código para trabalho deve ser feita através do [GitHub](https://github.com).

Brevemente, o repositório de cada grupo estará disponível em: https://github.com/tecnico-distsys/GXX-TupleSpaces/ (substituir `GXX` pelo
identificador de grupo).

A atualização do repositório deve ser feita com regularidade, correspondendo à distribuição de trabalho entre os membros
da equipa e às várias etapas de desenvolvimento.

Cada elemento do grupo deve atualizar o repositório do seu grupo à medida que vai concluindo as várias tarefas que lhe
foram atribuídas.

### Entregas


As entregas do projeto serão feitas também através do repositório GitHub.

A cada fase estará associada uma [*tag*](https://git-scm.com/book/en/v2/Git-Basics-Tagging).

As *tags* associadas a cada fase são `SD_F1`, `SD_F2` e `SD_F3`, respetivamente.
Cada grupo tem que marcar o código que representa cada entrega a realizar com uma *tag* específica antes
da hora limite de entrega.

As datas limites de entrega estão definidas no site dos laboratórios: (https://tecnico-distsys.github.io)


### Qualidade do código

A avaliação da qualidade engloba os seguintes aspetos:

- Configuração correta (POMs);

- Código legível (incluindo comentários relevantes);

- [Tratamento de exceções adequado]([https://tecnico-distsys.github.io/04-rpc-error-test/index.html](https://tecnico-distsys.github.io/04-rpc-error/grpc-error/index.html));

- [Sincronização correta](https://tecnico-distsys.github.io/02-tools-sockets/java-synch/index.html);

- Separação das classes geradas pelo protoc/gRPC das classes de domínio mantidas no servidor.

### Instalação e demonstração

As instruções de instalação e configuração de todo o sistema, de modo a que este possa ser colocado em funcionamento,
devem ser colocadas no documento `README.md`.

Este documento tem de estar localizado na raiz do projeto e tem que ser escrito em formato *[
MarkDown](https://guides.github.com/features/mastering-markdown/)*.



### Discussão

As notas das várias partes são indicativas e sujeitas a confirmação na discussão final, na qual todo o trabalho
desenvolvido durante o semestre será tido em conta.

As notas a atribuir serão individuais, por isso é importante que a divisão de tarefas ao longo do trabalho seja
equilibrada pelos membros do grupo.

Todas as discussões e revisões de nota do trabalho devem contar com a participação obrigatória de todos os membros do
grupo.


**Bom trabalho!**
 
