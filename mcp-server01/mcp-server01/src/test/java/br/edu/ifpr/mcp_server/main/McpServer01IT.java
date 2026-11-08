package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import tools.jackson.databind.json.JsonMapper;

class McpServer01IT {
   private static final Logger LOG =
                                   LoggerFactory.getLogger(McpServer01IT.class);
   // nome e caminho para o servidor MCP
   private static final String SERVER_PATH = "server/mcp_server01_server.jar";

   private static McpSyncClient client;

   @BeforeAll
   static void setUpBeforeClass() throws Exception {
      // antes que a classe de testes seja carregada, este método providencia
      // alguma inicialização ou alocação de recursos, neste caso, o servidor
      // MCP é executado e o cliente se conecta a ele
      File serverJar = new File(SERVER_PATH);
      assertTrue(serverJar.isFile(),"JAR do servidor não encontrado: " +
                                    serverJar.getAbsolutePath());

      String           jarPath = serverJar.getAbsolutePath();
      ServerParameters params  = ServerParameters.builder("java")
    			                                 .args("-jar",jarPath)
    			                                 .build();
      JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(
    			                                              new JsonMapper());
      StdioClientTransport transport  = new StdioClientTransport(params,
    			                                                 jsonMapper);

      transport.setStdErrorHandler(line -> LOG.info("[SERVER STDERR] " + line));

      client = McpClient.sync(transport).build();
      client.initialize();
   }

   @AfterAll
   static void tearDownAfterClass() throws Exception {
      // ao terminar todos os testes, todos os métodos nesta classe foram
      // executados, o cliente se desconecta do servidor
      if (client != null)
         client.closeGracefully();
   }

   // ==========================================================================
   // Os métodos de teste à seguir simulam a aplicação cliente:
   // - há ferramentas (tools) disponíveis???
   // - a ferramenta 'obter_documento' existe???
   // - a ferramenta 'obter_documento' executou com sucesso???
   // ==========================================================================

   @Test
   void deveDisponibilizarFerramentas() {
      ListToolsResult result = client.listTools();

      assertNotNull(result,"O servidor não retornou o resultado.");
      assertNotNull(result.tools(),"A lista de ferramentas não pode ser null.");
      assertFalse(result.tools().isEmpty(),"O servidor deveria disponibilizar" +
                                           " pelo menos uma ferramenta." );
   }

   @Test
   void deveDisponibilizarObterDocumento() {
      ListToolsResult result = client.listTools();
                                      // retorna a lista de ferramentas
      Tool            tool   = result.tools()
                                     // cria um fluxo para percorrer a lista
    			                     .stream()
    			                     // filtra a lista para um elemento
    			                     .filter(item ->
    			                          "obter_documento".equals(item.name()))
    			                     // verifica se há alguma elemento...
    			                     .findFirst()
    			                     // ...caso contrário, retorna "null"
    			                     .orElse(null);

      assertNotNull(tool,"A ferramenta obter_documento não foi encontrada.");
      assertNotNull(tool.description(),"A ferramenta deveria possuir " +
                                       "descrição.");
      assertNotNull(tool.inputSchema(),"A ferramenta deveria possuir " +
                                       "'inputSchema'.");
   }

   @Test
   void deveExecutarObterDocumento() {
      CallToolRequest request = CallToolRequest.builder("obter_documento")
    			                               .arguments(Map.of("id",123))
    			                               .build();
      CallToolResult  result  = client.callTool(request);

      assertNotNull(result,"O servidor não retornou resultado.");
      assertFalse(result.isError(),"A execução da ferramenta não deveria " +
                                   "resultar em erro.");
      assertNotNull(result.content(),"O resultado deveria possuir 'content'.");
      assertFalse(result.content().isEmpty(),"O resultado deveria possuir " +
                                             "pelo menos um 'content'." );
   }
}
