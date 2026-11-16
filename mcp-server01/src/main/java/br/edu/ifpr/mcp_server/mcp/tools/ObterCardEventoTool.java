
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.ifpr.mcp_server.Service.EventService;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ObterCardEventoTool implements McpToolDefinition {

    private final EventService eventService;

    public ObterCardEventoTool() {
        this(new EventService());
    }

    public ObterCardEventoTool(EventService eventService) {

        if (eventService == null) {
            throw new IllegalArgumentException(
                    "O serviço de eventos deve ser informado."
            );
        }

        this.eventService = eventService;
    }

    @Override
    public void register(
            SyncSpecification<SingleSessionSyncSpecification> mcp)
            throws Exception {

        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "evento_id_ou_slug", Map.of(
                                "type", "string",
                                "description",
                                "ID ou identificador (slug) do evento "
                                + "obtido através da ferramenta listar_eventos."
                        )
                ),
                "required", List.of("evento_id_ou_slug"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder(
                "obter_card_evento",
                inputSchema
        )
                .title("Obter card do evento")
                .description(
                        "Consulta todas as lutas de um evento de MMA, "
                        + "incluindo os lutadores participantes, ordem "
                        + "do card, categorias de peso e resultados "
                        + "quando disponíveis. Recebe o ID ou slug "
                        + "do evento."
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

            Object rawEventId =
                    arguments.get("evento_id_ou_slug");

            if (!(rawEventId instanceof String eventIdOrSlug)
                    || eventIdOrSlug.isBlank()) {

                return errorResult(
                        "Informe o ID ou identificador do evento "
                        + "no parâmetro 'evento_id_ou_slug'."
                );
            }

            JsonNode response = eventService.getEventBouts(
                    eventIdOrSlug.trim()
            );

            if (response == null || response.isNull()) {

                return errorResult(
                        "A Cito API não retornou informações sobre "
                        + "o card solicitado."
                );
            }

            String message = formatResult(
                    eventIdOrSlug.trim(),
                    response
            );

            TextContent content = TextContent.builder(message)
                    .build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return errorResult(
                    "A consulta do card foi interrompida. "
                    + "Tente novamente."
            );

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException ex) {

            return errorResult(ex.getMessage());

        } catch (Exception ex) {

            return errorResult(
                    "Não foi possível consultar o card do evento: "
                    + ex.getMessage()
            );
        }
    }

    private String formatResult(
            String eventIdOrSlug,
            JsonNode response) {

        StringBuilder result = new StringBuilder();

        result.append("CARD DO EVENTO")
                .append("\nIdentificador: ")
                .append(eventIdOrSlug);

        JsonNode bouts = findBoutsArray(response);

        if (bouts != null) {

            result.append("\nLutas encontradas: ")
                    .append(bouts.size());

            if (bouts.isEmpty()) {

                result.append(
                        "\nNenhuma luta foi encontrada para este evento."
                );
            }
        }

        result.append("\n\nDADOS DO CARD:")
                .append("\n")
                .append(response.toPrettyString());

        return result.toString();
    }

    private JsonNode findBoutsArray(JsonNode response) {

        JsonNode data = response.path("data");

        if (data.isArray()) {
            return data;
        }

        if (data.isObject()) {

            JsonNode bouts = data.path("bouts");

            if (bouts.isArray()) {
                return bouts;
            }

            JsonNode fights = data.path("fights");

            if (fights.isArray()) {
                return fights;
            }
        }

        JsonNode bouts = response.path("bouts");

        if (bouts.isArray()) {
            return bouts;
        }

        JsonNode fights = response.path("fights");

        if (fights.isArray()) {
            return fights;
        }

        return null;
    }

    private CallToolResult errorResult(String message) {

        TextContent content = TextContent.builder(
                message == null || message.isBlank()
                        ? "Ocorreu um erro ao consultar o card do evento."
                        : message
        ).build();

        return CallToolResult.builder()
                .content(List.of(content))
                .isError(true)
                .build();
    }
}
