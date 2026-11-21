
# MMA MCP

Servidor e cliente MCP desenvolvidos em Java para disponibilizar
consultas sobre lutadores, eventos e combates de MMA (especialmente sobre o UFC) através de
integrações com APIs externas.

O projeto utiliza o Model Context Protocol (MCP) para permitir que
assistentes de inteligência artificial e clientes compatíveis
acessem informações de MMA por meio de ferramentas especializadas.

## 1. Ferramentas

O servidor disponibiliza quatro ferramentas:

### buscar_lutador

Pesquisa lutadores pelo nome ou parte do nome.

**Parâmetro:**

| Nome | Tipo | Obrigatório |
|---|---|---|
| nome | string | Sim |

**Informações retornadas:**

- ID do lutador;
- Nome e apelido;
- Categoria de peso;
- País;
- Data de nascimento;
- Retrospecto profissional;
- Quantidade de lutas;
- Data da última luta;
- Quantidade de lutas no UFC.

**Possíveis resultados e erros:**

- Lista de lutadores encontrados;
- Nenhum lutador encontrado;
- Nome não informado ou vazio;
- Chave de API inválida ou ausente;
- Créditos insuficientes;
- Limite de requisições excedido;
- Falha de comunicação com a FighterAPI.

**API utilizada:** FighterAPI.

### listar_eventos

Consulta eventos de MMA e permite filtrar os próximos eventos.

**Parâmetros:**

| Nome | Tipo | Obrigatório | Padrão |
|---|---|---|---|
| pagina | integer | Não | 1 |
| limite | integer | Não | 10 |
| somente_futuros | boolean | Não | false |
| possui_estatisticas | boolean | Não | Não informado |

**Informações retornadas:**

- Identificador e nome do evento;
- Data e horário;
- Localização;
- Status;
- Disponibilidade de estatísticas;
- Informações de paginação.

**Possíveis resultados e erros:**

- Lista de eventos encontrados;
- Nenhum evento encontrado;
- Nenhum próximo evento disponível;
- Página ou limite inválido;
- Parâmetros booleanos inválidos;
- Chave de API inválida ou ausente;
- Limite de requisições excedido;
- Falha de comunicação com a Cito API.

**API utilizada:** Cito API.

### obter_card_evento

Consulta as lutas de um evento específico.

**Parâmetro:**

| Nome | Tipo | Obrigatório |
|---|---|---|
| evento_id_ou_slug | string | Sim |

Aceita o ID ou o identificador (slug) de um evento obtido através
da ferramenta listar_eventos.

Retorna os dados das lutas disponibilizados pela API, mantendo
a resposta JSON original.

**Possíveis resultados e erros:**

- Card do evento encontrado;
- Evento sem lutas disponíveis;
- Identificador não informado ou vazio;
- Evento não encontrado;
- Chave de API inválida ou ausente;
- Limite de requisições excedido;
- Falha de comunicação com a Cito API.

**API utilizada:** Cito API.

### obter_historico_lutas

Consulta as lutas recentes de um lutador.

**Parâmetro:**

| Nome | Tipo | Obrigatório |
|---|---|---|
| fighter_id | string | Sim |

O ID pode ser obtido através da ferramenta buscar_lutador.

**Informações retornadas quando disponíveis:**

- Evento;
- Data da luta;
- Categoria de peso;
- Vencedor;
- Método de encerramento;
- Round final;
- Tempo final.

**Possíveis resultados e erros:**

- Histórico de lutas encontrado;
- Nenhuma luta recente encontrada;
- Identificador não informado ou vazio;
- Perfil do lutador não encontrado;
- Chave de API inválida ou ausente;
- Créditos insuficientes;
- Limite de requisições excedido;
- Falha de comunicação com a FighterAPI.

**API utilizada:** FighterAPI.

---

## 2. Arquitetura

A arquitetura do projeto é dividida em camadas, separando
as ferramentas MCP das integrações com serviços externos.

```text
MMA_MCP
│
├── README.md
│
└── mcp-server01
    │
    ├── pom.xml
    ├── Dockerfile
    ├── compose.yaml
    ├── .env.example
    │
    └── src
        │
        ├── main
        │   ├── java
        │   │   └── br/edu/ifpr/mcp_server
        │   │       │
        │   │       ├── api
        │   │       │   ├── CitoApiClient.java
        │   │       │   └── FighterApiClient.java
        │   │       │
        │   │       ├── dto
        │   │       │
        │   │       ├── Service
        │   │       │   ├── EventService.java
        │   │       │   └── FighterService.java
        │   │       │
        │   │       ├── main
        │   │       │   ├── McpServer01SyncStdio.java
        │   │       │   └── McpClient01SyncStdio.java
        │   │       │
        │   │       └── mcp
        │   │           ├── tools
        │   │           └── utils
        │   │
        │   └── resources
        │
        └── test
            └── java
                └── br/edu/ifpr/mcp_server
                    ├── main
                    ├── Service
                    └── mcp/tools
```

### Fluxo de execução

```text
Cliente MCP
    |
    | STDIO
    v
Servidor MCP
    |
    v
Ferramenta MCP
    |
    v
Camada de serviços
    |
    v
Cliente da API
    |
    +--------------------+
    |                    |
    v                    v
 FighterAPI           Cito API
    |                    |
    +--------------------+
             |
             v
       Resultado MCP
```

## 3. Tecnologias utilizadas

- Java 25
- Apache Maven
- MCP Java SDK 2.0.1
- JUnit 6
- Jackson
- SLF4J
- Logback
- Docker
- Docker Compose
- MCP Inspector

## 4. APIs utilizadas

### FighterAPI

Utilizada nas ferramentas:

- buscar_lutador;
- obter_historico_lutas.

Variável de ambiente:

`FIGHTERAPI_MMA_KEY`

Website para cadastro: https://fighterapi.com/docs/atlas

### Cito API

Utilizada nas ferramentas:

- listar_eventos;
- obter_card_evento.

Variável de ambiente:

`CITOAPI_MMA_KEY`

Website para cadastro: https://citoapi.com/docs/api/ufc/ 

As credenciais devem ser fornecidas através de variáveis de
ambiente. Nunca devem ser inseridas diretamente no código-fonte
ou publicadas no repositório.

---

## 5. Configuração do ambiente

### Requisitos

- JDK 25;
- Apache Maven;
- Docker Engine;
- Docker Compose;
- Chaves de acesso às APIs utilizadas.

## 6. Compilação do servidor MCP

Na raiz do projeto Maven (`mcp-server01`), execute:

```bash
mvn clean verify -Pserver
```

O comando realiza a compilação, executa os testes e prepara
o pacote executável do servidor.

Após a conclusão, será criado:

```text
server/
├── mcp_server01_server.jar
└── libs/
```

O diretório `libs` possuei as dependências necessárias para a execução
do servidor.

---

## 7. Compilação do cliente MCP

Após gerar o servidor, compile o cliente:

```bash
mvn clean package -Pclient -DskipTests
```

Será gerado:

```text
target/
└── dist-client/
    ├── mcp_server01_client.jar
    └── libs/
```

### Executando o cliente

Entre no diretório do cliente:

```bash
cd target/dist-client
```

Execute:

```bash
java -jar mcp_server01_client.jar
```

O cliente iniciará automaticamente o servidor MCP e apresentará
as ferramentas disponíveis.

O usuário poderá selecionar uma ferramenta pelo número ou nome.

**Comandos disponíveis:**

- `listar`: atualiza a lista de ferramentas;
- `sair`: encerra o cliente.

Para executar as consultas, as variáveis de ambiente precisam
estar disponíveis no ambiente do processo Java.

No terminal Bash:

```bash
export FIGHTERAPI_MMA_KEY="sua_chave_fighterapi"
export CITOAPI_MMA_KEY="sua_chave_cito"
```

---

## 8. Executando com MCP Inspector

O projeto possui Dockerfile e compose.yaml para executar
o MCP Inspector em um contêiner Docker.

Primeiro, certifique-se de que o servidor já foi compilado.

Entre no diretório `mcp-server01` e execute:

```bash
docker compose up --build -d
```

Verifique o estado:

```bash
docker compose ps
```

Acesse no navegador:

http://127.0.0.1:6274

Na interface do MCP Inspector:

1. Conecte ao servidor Java;
2. Acesse a seção Tools;
3. Selecione a ferramenta desejada;
4. Informe seus parâmetros;
5. Execute a ferramenta.

Para visualizar os logs:

```bash
docker compose logs -f
```

Para encerrar:

```bash
docker compose down
```

---

## 9. Integração com Claude Desktop

O servidor também pode ser utilizado por clientes MCP
compatíveis com transporte STDIO.

Adicione a seguinte configuração à propriedade `mcpServers`
do arquivo de configuração do Claude Desktop:

```json
{
  "mcpServers": {
    "mma-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/caminho/absoluto/mcp-server01/server/mcp_server01_server.jar"
      ],
      "env": {
        "FIGHTERAPI_MMA_KEY": "sua_chave_fighterapi",
        "CITOAPI_MMA_KEY": "sua_chave_cito"
      }
    }
  }
}
```

Substitua o caminho e as credenciais pelos valores do ambiente
local.

Após salvar a configuração, reinicie o Claude Desktop.

---

## 10. Testes

O projeto possui testes unitários e testes de integração.

### Testes unitários

Os testes verificam:

- Validação dos parâmetros;
- Comportamento dos serviços;
- Formatação dos resultados;
- Tratamento de respostas vazias;
- Tratamento de erros.

As respostas das APIs são simuladas nos testes unitários,
evitando chamadas externas desnecessárias.

### Testes de integração

Os testes verificam:

- Inicialização do servidor MCP;
- Comunicação entre cliente e servidor;
- Disponibilização das quatro ferramentas;
- Nomes e esquemas dos parâmetros.

### Testes manuais das APIs

O projeto também mantém as classes auxiliares:

- `TestFighterApi.java`;
- `TestCitoApi.java`.

Elas permitem executar consultas diretamente nas APIs,
utilizando as respectivas variáveis de ambiente.

---

## 11. Considerações

- O transporte utilizado entre o cliente e o servidor é STDIO.
- O cliente próprio consulta dinamicamente as ferramentas
  registradas no servidor.
- As integrações HTTP ficam isoladas nas classes da camada API.
- As ferramentas MCP não acessam diretamente os endpoints.
- As credenciais são obtidas por variáveis de ambiente.
- Os diretórios `target` e `server` são gerados durante o build.

Ao executar novamente o build do servidor, o Maven pode remover
os artefatos anteriores do diretório `target`. Por isso, gere
o servidor antes do cliente.

---

## 12. Referências

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [MCP Inspector](https://github.com/modelcontextprotocol/inspector)
- [Apache Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/)
