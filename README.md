
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

A ferramenta utiliza o histórico recente disponibilizado pela
FighterAPI, não necessariamente toda a carreira do atleta.

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
(A cito API possui uma chave de demonstração, permitindo 50 requisições por dia e não sendo necessário o cadastro. pk_demo_cito_live_a06c129e0a9ce1c39c3035bd187541c4)


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

## 12. Referências

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [MCP Inspector](https://github.com/modelcontextprotocol/inspector)
- [Apache Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/)
