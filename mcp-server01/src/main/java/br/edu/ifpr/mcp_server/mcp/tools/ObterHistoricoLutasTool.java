package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.Service.FighterService;
import br.edu.ifpr.mcp_server.dto.FighterProfile;
import br.edu.ifpr.mcp_server.dto.FighterProfile.RecentFight;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ObterHistoricoLutasTool implements McpToolDefinition {

    private final FighterService fighterService;

    public ObterHistoricoLutasTool() {
        this(new FighterService());
    }

    public ObterHistoricoLutasTool(FighterService fighterService) {

        if (fighterService == null) {
            throw new IllegalArgumentException(
                    "O serviço de lutadores deve ser informado."
            );
        }

        this.fighterService = fighterService;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "fighter_id", Map.of(
                                "type", "string",
                                "description",
                                "Identificador do lutador obtido "
                                + "pela ferramenta buscar_lutador."
                        )
                ),
                "required", List.of("fighter_id"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder(
                "obter_historico_lutas",
                inputSchema
        )
                .title("Obter histórico de lutas")
                .description(
                        "Consulta as lutas recentes de um lutador, "
                        + "apresentando eventos, datas, categorias, "
                        + "vencedores, métodos de encerramento e "
                        + "informações sobre o round final, "
                        + "quando disponíveis."
                )
                .build();

        SyncToolSpecification toolSpec =
                SyncToolSpecification.builder()
                        .tool(tool)
                        .callHandler(this::run)
                        .build();

        mcp.tools(toolSpec);
    }

    @Override
    public CallToolResult run(
            McpSyncServerExchange exchange,
            CallToolRequest request) {

        try {

            Map<String, Object> arguments =
                    request.arguments() == null
                            ? Map.of()
                            : request.arguments();

            Object rawFighterId = arguments.get("fighter_id");

            if (!(rawFighterId instanceof String fighterId)
                    || fighterId.isBlank()) {

                return errorResult(
                        "Informe o identificador do lutador "
                        + "no parâmetro 'fighter_id'."
                );
            }

            FighterProfile profile =
                    fighterService.getFighterProfile(
                            fighterId.trim()
                    );

            if (profile == null) {

                return errorResult(
                        "Não foi possível obter o perfil do lutador."
                );
            }

            String message = formatResult(
                    fighterId.trim(),
                    profile
            );

            TextContent content = TextContent.builder(message)
                    .build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return errorResult(
                    "A consulta do histórico foi interrompida. "
                    + "Tente novamente."
            );

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException ex) {

            return errorResult(ex.getMessage());

        } catch (Exception ex) {

            return errorResult(
                    "Não foi possível consultar o histórico: "
                    + ex.getMessage()
            );
        }
    }

    private String formatResult(
            String fighterId,
            FighterProfile profile) {

        String fighterName = fighterId;

        if (profile.fighter() != null
                && profile.fighter().name() != null
                && !profile.fighter().name().isBlank()) {

            fighterName = profile.fighter().name();
        }

        List<RecentFight> fights = profile.recentFights();

        if (fights == null || fights.isEmpty()) {

            return "Nenhuma luta recente foi encontrada para "
                    + fighterName + ".";
        }

        StringBuilder result = new StringBuilder();

        result.append("HISTÓRICO DE LUTAS RECENTES")
                .append("\nLutador: ")
                .append(fighterName)
                .append("\nID: ")
                .append(fighterId)
                .append("\nTotal de lutas encontradas: ")
                .append(fights.size());

        for (int index = 0; index < fights.size(); index++) {

            RecentFight fight = fights.get(index);

            if (fight == null) {
                continue;
            }

            result.append("\n\n")
                    .append(index + 1)
                    .append(". ")
                    .append(
                            fight.eventName() == null
                                    || fight.eventName().isBlank()
                                    ? "Evento não informado"
                                    : fight.eventName()
                    );

            appendField(result, "ID da luta", fight.boutId());
            appendField(result, "ID do evento", fight.eventId());
            appendField(result, "Data", fight.eventDate());
            appendField(result, "Categoria", fight.weightClass());
            appendField(result, "Vencedor", fight.winner());

            String method = fight.methodDisplay();

            if (method == null || method.isBlank()) {
                method = fight.method();
            }

            appendField(result, "Método de encerramento", method);
            appendField(result, "Round final", fight.endRound());
            appendField(result, "Tempo final", fight.endClock());
        }

        return result.toString();
    }

    private void appendField(
            StringBuilder result,
            String label,
            Object value) {

        if (value != null && !value.toString().isBlank()) {

            result.append("\n   ")
                    .append(label)
                    .append(": ")
                    .append(value);
        }
    }

    private CallToolResult errorResult(String message) {

        TextContent content = TextContent.builder(
                message == null || message.isBlank()
                        ? "Ocorreu um erro ao consultar o histórico de lutas."
                        : message
        ).build();

        return CallToolResult.builder()
                .content(List.of(content))
                .isError(true)
                .build();
    }
}
