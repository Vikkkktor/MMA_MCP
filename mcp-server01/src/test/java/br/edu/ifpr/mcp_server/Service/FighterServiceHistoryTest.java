package br.edu.ifpr.mcp_server.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterProfile;
import br.edu.ifpr.mcp_server.dto.FighterProfile.RecentFight;

class FighterServiceHistoryTest {

    @Test
    void deveConsultarHistoricoDoLutador() throws Exception {

        StubFighterApiClient apiClient =
                new StubFighterApiClient();

        FighterService service =
                new FighterService(apiClient);

        service.getFighterProfile("  fighter-123  ");

        assertEquals(
                "fighter-123",
                apiClient.receivedFighterId
        );
    }

    @Test
    void deveRejeitarIdentificadorVazio() {

        FighterService service = new FighterService(
                new StubFighterApiClient()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getFighterProfile("   ")
        );
    }

    private static class StubFighterApiClient
            extends FighterApiClient {

        private String receivedFighterId;

        StubFighterApiClient() {
            super("test-api-key");
        }

        @Override
        public FighterProfile getFighter(String fighterId) {

            receivedFighterId = fighterId;

            return createProfile();
        }

        private FighterProfile createProfile() {

            FighterProfile.FighterBio fighter =
                    new FighterProfile.FighterBio(
                            "fighter-123",
                            "Charles Oliveira",
                            "Do Bronx",
                            null,
                            null,
                            null,
                            null,
                            "Orthodox",
                            "Brazil",
                            "Chute Boxe",
                            "Male",
                            "Lightweight",
                            "35-10-0"
                    );

            RecentFight recentFight = new RecentFight(
                    "bout-123",
                    "event-123",
                    "UFC 309",
                    "2024-11-16",
                    "Lightweight",
                    "Charles Oliveira",
                    "Submission",
                    "Submission",
                    2,
                    "2:10"
            );

            return new FighterProfile(
                    fighter,
                    null,
                    null,
                    null,
                    null,
                    List.of(recentFight)
            );
        }
    }
}
