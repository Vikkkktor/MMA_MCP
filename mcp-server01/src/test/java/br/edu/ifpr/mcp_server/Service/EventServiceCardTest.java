package br.edu.ifpr.mcp_server.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.api.CitoApiClient;

class EventServiceCardTest {

    @Test
    void deveConsultarCardPorIdentificador() throws Exception {

        StubCitoApiClient apiClient =
                new StubCitoApiClient();

        EventService service =
                new EventService(apiClient);

        JsonNode response = service.getEventBouts("ufc-300");

        assertTrue(response.path("success").asBoolean());

        assertEquals(
                "Charles Oliveira",
                response.path("data")
                        .get(0)
                        .path("fighter1")
                        .asText()
        );

        assertEquals(
                "ufc-300",
                apiClient.receivedEventId
        );
    }

    @Test
    void deveRemoverEspacosDoIdentificador() throws Exception {

        StubCitoApiClient apiClient =
                new StubCitoApiClient();

        EventService service =
                new EventService(apiClient);

        service.getEventBouts("  ufc-300  ");

        assertEquals(
                "ufc-300",
                apiClient.receivedEventId
        );
    }

    @Test
    void deveRejeitarIdentificadorVazio() {

        EventService service = new EventService(
                new StubCitoApiClient()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getEventBouts("   ")
        );
    }

    private static class StubCitoApiClient
            extends CitoApiClient {

        private String receivedEventId;

        private final JsonNode response;

        StubCitoApiClient() {

            super("test-api-key");

            try {

                response = new ObjectMapper().readTree("""
                        {
                            "success": true,
                            "data": [
                                {
                                    "id": "bout-123",
                                    "fighter1": "Charles Oliveira",
                                    "fighter2": "Michael Chandler",
                                    "weightClass": "Lightweight"
                                }
                            ]
                        }
                        """);

            } catch (IOException ex) {

                throw new IllegalStateException(ex);
            }
        }

        @Override
        public JsonNode getEventBouts(String eventIdOrSlug) {

            receivedEventId = eventIdOrSlug;

            return response;
        }
    }
}
