package br.edu.ifpr.mcp_server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.Service.EventService;
import br.edu.ifpr.mcp_server.api.CitoApiClient;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class ObterCardEventoToolTest {

    private JsonNode createResponse() throws IOException {

        return new ObjectMapper().readTree("""
                {
                    "success": true,
                    "data": [
                        {
                            "id": "bout-123",
                            "fighter1": "Charles Oliveira",
                            "fighter2": "Michael Chandler",
                            "weightClass": "Lightweight",
                            "result": {
                                "winner": "Charles Oliveira",
                                "method": "Submission"
                            }
                        },
                        {
                            "id": "bout-456",
                            "fighter1": "Fighter A",
                            "fighter2": "Fighter B",
                            "weightClass": "Welterweight"
                        }
                    ]
                }
                """);
    }

    @Test
    void deveRetornarCardDoEvento() throws Exception {

        StubEventService service =
                new StubEventService(createResponse());

        ObterCardEventoTool tool =
                new ObterCardEventoTool(service);

        CallToolResult result = tool.run(
                null,
                request("ufc-300")
        );

        assertFalse(result.isError());

        assertEquals(
                "ufc-300",
                service.receivedEventId
        );

        String text = textFrom(result);

        assertTrue(text.contains("CARD DO EVENTO"));
        assertTrue(text.contains("Lutas encontradas: 2"));
        assertTrue(text.contains("Charles Oliveira"));
        assertTrue(text.contains("Michael Chandler"));
        assertTrue(text.contains("Lightweight"));
        assertTrue(text.contains("Submission"));
    }

    @Test
    void deveAceitarSlugDoEvento() throws Exception {

        StubEventService service =
                new StubEventService(createResponse());

        ObterCardEventoTool tool =
                new ObterCardEventoTool(service);

        CallToolResult result = tool.run(
                null,
                request("ufc-300-oliveira-vs-gaethje")
        );

        assertFalse(result.isError());

        assertEquals(
                "ufc-300-oliveira-vs-gaethje",
                service.receivedEventId
        );
    }

    @Test
    void deveRetornarErroQuandoIdentificadorNaoForInformado()
            throws Exception {

        StubEventService service =
                new StubEventService(createResponse());

        ObterCardEventoTool tool =
                new ObterCardEventoTool(service);

        CallToolResult result = tool.run(
                null,
                request("   ")
        );

        assertTrue(result.isError());
    }

    @Test
    void deveInformarQuandoCardNaoPossuirLutas()
            throws Exception {

        JsonNode emptyResponse = new ObjectMapper().readTree("""
                {
                    "success": true,
                    "data": []
                }
                """);

        ObterCardEventoTool tool =
                new ObterCardEventoTool(
                        new StubEventService(emptyResponse)
                );

        CallToolResult result = tool.run(
                null,
                request("ufc-evento-teste")
        );

        assertFalse(result.isError());

        assertTrue(
                textFrom(result).contains(
                        "Lutas encontradas: 0"
                )
        );

        assertTrue(
                textFrom(result).contains(
                        "Nenhuma luta foi encontrada"
                )
        );
    }

    private CallToolRequest request(String eventIdOrSlug) {

        return CallToolRequest.builder("obter_card_evento")
                .arguments(Map.of(
                        "evento_id_ou_slug",
                        eventIdOrSlug
                ))
                .build();
    }

    private String textFrom(CallToolResult result) {

        return ((TextContent) result.content().get(0)).text();
    }

    private static class StubEventService extends EventService {

        private final JsonNode response;

        private String receivedEventId;

        StubEventService(JsonNode response) {

            super(new CitoApiClient("test-api-key"));

            this.response = response;
        }

        @Override
        public JsonNode getEventBouts(String eventIdOrSlug) {

            receivedEventId = eventIdOrSlug;

            return response;
        }
    }
}