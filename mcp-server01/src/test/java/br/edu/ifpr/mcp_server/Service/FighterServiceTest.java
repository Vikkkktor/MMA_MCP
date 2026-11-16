package br.edu.ifpr.mcp_server.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;

class FighterServiceTest {

    @Test
    void deveRemoverEspacosAntesDeConsultarApi() throws Exception {

        StubFighterApiClient apiClient =
                new StubFighterApiClient();

        FighterService service =
                new FighterService(apiClient);

        service.searchFighters("  Charles Oliveira  ");

        assertEquals(
                "Charles Oliveira",
                apiClient.receivedQuery
        );
    }

    @Test
    void deveRejeitarNomeVazio() {

        FighterService service = new FighterService(
                new StubFighterApiClient()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.searchFighters("   ")
        );
    }

    private static class StubFighterApiClient
            extends FighterApiClient {

        private String receivedQuery;

        StubFighterApiClient() {

            super("test-api-key");
        }

        @Override
        public List<FighterSearchResult> searchFighters(
                String query) {

            receivedQuery = query;

            return List.of();
        }
    }
}
