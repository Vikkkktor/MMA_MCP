package br.edu.ifpr.mcp_server.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.api.CitoApiClient;
import br.edu.ifpr.mcp_server.dto.EventResponse;

class EventServiceTest {

    private static final String RESPONSE_JSON = """
            {
                "success": true,
                "data": [
                    {
                        "id": "event-123",
                        "slug": "evento-teste",
                        "title": "Evento de teste",
                        "shortTitle": "Teste",
                        "status": "scheduled",
                        "isLive": false,
                        "startsAt": "2027-01-01T20:00:00Z",
                        "eventDate": "2027-01-01",
                        "eventDateLabel": "01/01/2027",
                        "venue": "Arena",
                        "city": "Curitiba",
                        "state": "PR",
                        "country": "Brasil",
                        "locationText": "Arena, Curitiba, PR",
                        "hasStats": true
                    }
                ],
                "meta": {
                    "page": 2,
                    "limit": 5,
                    "total": 20,
                    "totalPages": 4,
                    "hasNextPage": true,
                    "hasPreviousPage": true,
                    "nextPage": 3,
                    "previousPage": 1
                }
            }
            """;

    @Test
    void deveConsultarEventosComSucesso() throws Exception {

        StubCitoApiClient apiClient =
                new StubCitoApiClient(RESPONSE_JSON);

        EventService service = new EventService(apiClient);

        EventResponse response = service.getEvents(
                2,
                5,
                true
        );

        assertNotNull(response);

        assertEquals(
                "Evento de teste",
                response.data().get(0).title()
        );

        assertEquals(2, apiClient.receivedPage);
        assertEquals(5, apiClient.receivedLimit);
        assertEquals(true, apiClient.receivedHasStats);
    }

    @Test
    void deveConsultarProximosEventos() throws Exception {

        StubCitoApiClient apiClient =
                new StubCitoApiClient(RESPONSE_JSON);

        EventService service = new EventService(apiClient);

        EventResponse response = service.getUpcomingEvents();

        assertNotNull(response);

        assertEquals(1, apiClient.upcomingCalls);

        assertEquals(
                "Evento de teste",
                response.data().get(0).title()
        );
    }

    @Test
    void deveRejeitarPaginaInvalida() throws Exception {

        StubCitoApiClient apiClient =
                new StubCitoApiClient(RESPONSE_JSON);

        EventService service = new EventService(apiClient);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getEvents(0, 10, null)
        );

        assertEquals(0, apiClient.eventCalls);
    }

    @Test
    void deveRejeitarLimiteInvalido() throws Exception {

        EventService service = new EventService(
                new StubCitoApiClient(RESPONSE_JSON)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getEvents(1, 0, null)
        );
    }

    private static class StubCitoApiClient extends CitoApiClient {

        private final JsonNode response;

        private int receivedPage;
        private int receivedLimit;
        private Boolean receivedHasStats;

        private int eventCalls;
        private int upcomingCalls;

        StubCitoApiClient(String json) throws IOException {

            super("test-api-key");

            this.response = new ObjectMapper().readTree(json);
        }

        @Override
        public JsonNode getEvents(
                int page,
                int limit,
                Boolean hasStats) {

            eventCalls++;

            receivedPage = page;
            receivedLimit = limit;
            receivedHasStats = hasStats;

            return response;
        }

        @Override
        public JsonNode getUpcomingEvents() {

            upcomingCalls++;

            return response;
        }
    }
}
