
package br.edu.ifpr.mcp_server.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
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
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import tools.jackson.databind.json.JsonMapper;

class McpServer01IT {

    private static final Logger LOG =
            LoggerFactory.getLogger(McpServer01IT.class);

    private static final String SERVER_PATH =
            "server/mcp_server01_server.jar";

    private static McpSyncClient client;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {

        File serverJar = new File(SERVER_PATH);

        assertTrue(
                serverJar.isFile(),
                "JAR do servidor não encontrado: "
                        + serverJar.getAbsolutePath()
        );

        String jarPath = serverJar.getAbsolutePath();

        ServerParameters params =
                ServerParameters.builder("java")
                        .args("-jar", jarPath)
                        .build();

        JacksonMcpJsonMapper jsonMapper =
                new JacksonMcpJsonMapper(new JsonMapper());

        StdioClientTransport transport =
                new StdioClientTransport(
                        params,
                        jsonMapper
                );

        transport.setStdErrorHandler(
                line -> LOG.info("[SERVER STDERR] " + line)
        );

        client = McpClient.sync(transport).build();

        client.initialize();
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {

        if (client != null) {
            client.closeGracefully();
        }
    }

    @Test
    void deveDisponibilizarFerramentas() {

        ListToolsResult result = client.listTools();

        assertNotNull(
                result,
                "O servidor não retornou o resultado."
        );

        assertNotNull(
                result.tools(),
                "A lista de ferramentas não pode ser null."
        );

        assertEquals(
                4,
                result.tools().size(),
                "O servidor deveria disponibilizar exatamente quatro ferramentas."
        );
    }

    @Test
    void deveDisponibilizarBuscarLutador() {

        Tool tool = obterFerramenta("buscar_lutador");

        assertNotNull(tool);
        assertNotNull(tool.description());
        assertNotNull(tool.inputSchema());

        assertTrue(tool.inputSchema().containsKey("properties"));

        assertEquals(
                List.of("nome"),
                tool.inputSchema().get("required")
        );
    }

    @Test
    void deveDisponibilizarListarEventos() {

        Tool tool = obterFerramenta("listar_eventos");

        assertNotNull(tool);
        assertNotNull(tool.description());
        assertNotNull(tool.inputSchema());

        assertTrue(tool.inputSchema().containsKey("properties"));
    }

    @Test
    void deveDisponibilizarObterCardEvento() {

        Tool tool = obterFerramenta("obter_card_evento");

        assertNotNull(tool);
        assertNotNull(tool.description());
        assertNotNull(tool.inputSchema());

        assertTrue(tool.inputSchema().containsKey("properties"));

        assertEquals(
                List.of("evento_id_ou_slug"),
                tool.inputSchema().get("required")
        );
    }

    @Test
    void deveDisponibilizarObterHistoricoLutas() {

        Tool tool = obterFerramenta("obter_historico_lutas");

        assertNotNull(tool);
        assertNotNull(tool.description());
        assertNotNull(tool.inputSchema());

        assertTrue(tool.inputSchema().containsKey("properties"));

        assertEquals(
                List.of("fighter_id"),
                tool.inputSchema().get("required")
        );
    }

    @Test
    void deveExecutarBuscarLutadorPeloMcp() {

        CallToolResult result = executarFerramenta(
                "buscar_lutador",
                Map.of("nome", " ")
        );

        String message = obterTexto(result);

        assertTrue(
                Boolean.TRUE.equals(result.isError()),
                "A ferramenta deveria rejeitar um nome vazio."
        );

        assertTrue(
                message.contains("nome do lutador"),
                "A resposta deveria informar que o nome é inválido."
        );
    }


	@Test
	void deveExecutarListarEventosPeloMcp() {
	
	    CallToolResult result = executarFerramenta(
	            "listar_eventos",
	            Map.of(
	                    "pagina", 0,
	                    "limite", 10
	            )
	    );
	
	    assertNotNull(
	            result,
	            "O servidor deveria retornar uma resposta."
	    );
	
	    assertTrue(
	            Boolean.TRUE.equals(result.isError()),
	            "A ferramenta deveria rejeitar uma página inválida."
	    );
	
	    String message = obterTexto(result);
	
	    assertFalse(
	            message.isBlank(),
	            "O servidor deveria informar o motivo da rejeição."
	    );
	}

    @Test
    void deveExecutarObterCardEventoPeloMcp() {

        CallToolResult result = executarFerramenta(
                "obter_card_evento",
                Map.of("evento_id_ou_slug", " ")
        );

        String message = obterTexto(result);

        assertTrue(
                Boolean.TRUE.equals(result.isError()),
                "A ferramenta deveria rejeitar um identificador vazio."
        );

        assertTrue(
                message.contains("evento_id_ou_slug"),
                "A resposta deveria identificar o parâmetro inválido."
        );
    }

    @Test
    void deveExecutarObterHistoricoLutasPeloMcp() {

        CallToolResult result = executarFerramenta(
                "obter_historico_lutas",
                Map.of("fighter_id", " ")
        );

        String message = obterTexto(result);

        assertTrue(
                Boolean.TRUE.equals(result.isError()),
                "A ferramenta deveria rejeitar um identificador vazio."
        );

        assertTrue(
                message.contains("fighter_id"),
                "A resposta deveria identificar o parâmetro inválido."
        );
    }

    private Tool obterFerramenta(String nome) {

        ListToolsResult result = client.listTools();

        assertNotNull(result);
        assertNotNull(result.tools());

        return result.tools().stream()
                .filter(tool -> nome.equals(tool.name()))
                .findFirst()
                .orElse(null);
    }

    private CallToolResult executarFerramenta(
            String nome,
            Map<String, Object> argumentos) {

        CallToolRequest request =
                CallToolRequest.builder(nome)
                        .arguments(argumentos)
                        .build();

        return client.callTool(request);
    }

    private String obterTexto(CallToolResult result) {

        assertNotNull(
                result,
                "O servidor não retornou uma resposta."
        );

        assertNotNull(
                result.content(),
                "A resposta não possui conteúdo."
        );

        assertFalse(
                result.content().isEmpty(),
                "A resposta deveria possuir pelo menos um conteúdo."
        );

        return result.content().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast)
                .map(TextContent::text)
                .findFirst()
                .orElse("");
    }
}
