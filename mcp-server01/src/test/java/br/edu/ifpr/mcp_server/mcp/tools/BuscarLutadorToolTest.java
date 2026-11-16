
package br.edu.ifpr.mcp_server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.Service.FighterService;
import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class BuscarLutadorToolTest {

    @Test
    void deveRetornarOsLutadoresEncontrados() {

        FighterSearchResult fighter = new FighterSearchResult(
                "fighter-123",
                "Charles Oliveira",
                "Do Bronx",
                "Lightweight",
                "Brazil",
                "1989-10-17",
                "34-10-0",
                44,
                "2024-10-05",
                30
        );

        StubFighterService service = new StubFighterService(
                List.of(fighter)
        );

        BuscarLutadorTool tool = new BuscarLutadorTool(service);

        CallToolResult result = tool.run(
                null,
                request("Charles")
        );

        assertFalse(result.isError());
        assertEquals("Charles", service.searchedName);

        String text = textFrom(result);

        assertTrue(text.contains("Foram encontrados 1 lutador(es)"));
        assertTrue(text.contains("Charles Oliveira"));
        assertTrue(text.contains("Do Bronx"));
        assertTrue(text.contains("34-10-0"));
    }

    @Test
    void deveInformarQuandoNenhumLutadorForEncontrado() {

        BuscarLutadorTool tool = new BuscarLutadorTool(
                new StubFighterService(List.of())
        );

        CallToolResult result = tool.run(
                null,
                request("Nome inexistente")
        );

        assertFalse(result.isError());

        assertTrue(textFrom(result).contains(
                "Nenhum lutador encontrado para: Nome inexistente"
        ));
    }

    @Test
    void deveRetornarErroQuandoNomeNaoForInformado() {

        BuscarLutadorTool tool = new BuscarLutadorTool(
                new StubFighterService(List.of())
        );

        CallToolRequest request =
                CallToolRequest.builder("buscar_lutador")
                        .arguments(Map.of())
                        .build();

        CallToolResult result = tool.run(null, request);

        assertTrue(result.isError());

        assertTrue(textFrom(result).contains(
                "informe o nome do lutador"
        ));
    }

    private CallToolRequest request(String name) {

        return CallToolRequest.builder("buscar_lutador")
                .arguments(Map.of("nome", name))
                .build();
    }

    private String textFrom(CallToolResult result) {

        return ((TextContent) result.content().get(0)).text();
    }

    private static class StubFighterService extends FighterService {

        private final List<FighterSearchResult> fighters;

        private String searchedName;

        StubFighterService(List<FighterSearchResult> fighters) {

            super(new FighterApiClient("test-api-key"));

            this.fighters = fighters;
        }

        @Override
        public List<FighterSearchResult> searchFighters(String name) {

            this.searchedName = name;

            return fighters;
        }
    }
}
