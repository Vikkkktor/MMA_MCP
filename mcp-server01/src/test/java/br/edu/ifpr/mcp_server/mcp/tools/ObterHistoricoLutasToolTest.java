package br.edu.ifpr.mcp_server.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.edu.ifpr.mcp_server.Service.FighterService;
import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterProfile;
import br.edu.ifpr.mcp_server.dto.FighterProfile.RecentFight;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

class ObterHistoricoLutasToolTest {

    private FighterProfile createProfile(
            List<RecentFight> recentFights) {

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

        return new FighterProfile(
                fighter,
                null,
                null,
                null,
                null,
                recentFights
        );
    }

    @Test
    void deveRetornarHistoricoDeLutas() {

        RecentFight fight = new RecentFight(
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

        StubFighterService service = new StubFighterService(
                createProfile(List.of(fight))
        );

        ObterHistoricoLutasTool tool =
                new ObterHistoricoLutasTool(service);

        CallToolResult result = tool.run(
                null,
                request("fighter-123")
        );

        assertFalse(result.isError());

        assertEquals(
                "fighter-123",
                service.receivedFighterId
        );

        String text = textFrom(result);

        assertTrue(text.contains("HISTÓRICO DE LUTAS RECENTES"));
        assertTrue(text.contains("Charles Oliveira"));
        assertTrue(text.contains("UFC 309"));
        assertTrue(text.contains("2024-11-16"));
        assertTrue(text.contains("Lightweight"));
        assertTrue(text.contains("Submission"));
        assertTrue(text.contains("2:10"));
    }

    @Test
    void deveInformarQuandoNaoExistiremLutasRecentes() {

        StubFighterService service = new StubFighterService(
                createProfile(List.of())
        );

        ObterHistoricoLutasTool tool =
                new ObterHistoricoLutasTool(service);

        CallToolResult result = tool.run(
                null,
                request("fighter-123")
        );

        assertFalse(result.isError());

        assertTrue(
                textFrom(result).contains(
                        "Nenhuma luta recente foi encontrada"
                )
        );
    }

    @Test
    void deveRetornarErroQuandoIdentificadorNaoForInformado() {

        StubFighterService service = new StubFighterService(
                createProfile(List.of())
        );

        ObterHistoricoLutasTool tool =
                new ObterHistoricoLutasTool(service);

        CallToolResult result = tool.run(
                null,
                request("   ")
        );

        assertTrue(result.isError());

        assertTrue(
                textFrom(result).contains(
                        "identificador do lutador"
                )
        );
    }

    private CallToolRequest request(String fighterId) {

        return CallToolRequest.builder("obter_historico_lutas")
                .arguments(Map.of("fighter_id", fighterId))
                .build();
    }

    private String textFrom(CallToolResult result) {

        return ((TextContent) result.content().get(0)).text();
    }

    private static class StubFighterService extends FighterService {

        private final FighterProfile profile;

        private String receivedFighterId;

        StubFighterService(FighterProfile profile) {

            super(new FighterApiClient("test-api-key"));

            this.profile = profile;
        }

        @Override
        public FighterProfile getFighterProfile(String fighterId) {

            receivedFighterId = fighterId;

            return profile;
        }
    }
}
