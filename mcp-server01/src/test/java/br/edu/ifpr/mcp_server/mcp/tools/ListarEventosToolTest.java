package br.edu.ifpr.mcp_server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.Service.EventService;
import br.edu.ifpr.mcp_server.api.CitoApiClient;
import br.edu.ifpr.mcp_server.dto.EventResponse;
import br.edu.ifpr.mcp_server.dto.EventSummary;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class ListarEventosToolTest {

    private EventResponse createResponse() {

        EventSummary event = new EventSummary(
                "event-123",
                "evento-teste",
                "Evento de teste",
                "Teste",
                "scheduled",
                false,
                "2027-01-01T20:00:00Z",
                "2027-01-01",
                "01/01/2027",
                "Arena",
                "Curitiba",
                "PR",
                "Brasil",
                "Arena, Curitiba, PR",
                null,
                true,
                "fresh"
        );

        EventResponse.Pagination pagination =
                new EventResponse.Pagination(
                        1,
                        10,
                        1,
                        1,
                        false,
                        false,
                        null,
                        null
                );

        return new EventResponse(
                true,
                List.of(event),
                pagination
        );
    }

    @Test
    void deveListarEventosComParametrosPadrao() {

        StubEventService service =
                new StubEventService(createResponse());

        ListarEventosTool tool =
                new ListarEventosTool(service);

        CallToolResult result = tool.run(
                null,
                request(Map.of())
        );

        assertFalse(result.isError());

        assertEquals(1, service.receivedPage);
        assertEquals(10, service.receivedLimit);
        assertEquals(null, service.receivedHasStats);

        String text = textFrom(result);

        assertTrue(text.contains("Evento de teste"));
        assertTrue(text.contains("event-123"));
        assertTrue(text.contains("Curitiba"));
    }

    @Test
    void deveConsultarSomenteProximosEventos() {

        StubEventService service =
                new StubEventService(createResponse());

        ListarEventosTool tool =
                new ListarEventosTool(service);

        CallToolResult result = tool.run(
                null,
                request(Map.of("somente_futuros", true))
        );

        assertFalse(result.isError());

        assertEquals(1, service.upcomingCalls);

        assertTrue(
                textFrom(result).contains("PRÓXIMOS EVENTOS")
        );
    }

    @Test
    void deveAceitarParametrosPersonalizados() {

        StubEventService service =
                new StubEventService(createResponse());

        ListarEventosTool tool =
                new ListarEventosTool(service);

        CallToolResult result = tool.run(
                null,
                request(Map.of(
                        "pagina", 2,
                        "limite", 5,
                        "possui_estatisticas", true
                ))
        );

        assertFalse(result.isError());

        assertEquals(2, service.receivedPage);
        assertEquals(5, service.receivedLimit);
        assertEquals(true, service.receivedHasStats);
    }

    @Test
    void deveRetornarErroParaPaginaInvalida() {

        StubEventService service =
                new StubEventService(createResponse());

        ListarEventosTool tool =
                new ListarEventosTool(service);

        CallToolResult result = tool.run(
                null,
                request(Map.of("pagina", 0))
        );

        assertTrue(result.isError());
    }

    private CallToolRequest request(
            Map<String, Object> arguments) {

        return CallToolRequest.builder("listar_eventos")
                .arguments(arguments)
                .build();
    }

    private String textFrom(CallToolResult result) {

        return ((TextContent) result.content().get(0)).text();
    }

    private static class StubEventService extends EventService {

        private final EventResponse response;

        private int receivedPage;
        private int receivedLimit;
        private Boolean receivedHasStats;

        private int upcomingCalls;

        StubEventService(EventResponse response) {

            super(new CitoApiClient("test-api-key"));

            this.response = response;
        }

        @Override
        public EventResponse getEvents(
                int page,
                int limit,
                Boolean hasStats) {

            receivedPage = page;
            receivedLimit = limit;
            receivedHasStats = hasStats;

            return response;
        }

        @Override
        public EventResponse getUpcomingEvents() {

            upcomingCalls++;

            return response;
        }
    }
}