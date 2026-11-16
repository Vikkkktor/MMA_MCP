package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import br.edu.ifpr.mcp_server.Service.EventService;
import br.edu.ifpr.mcp_server.dto.EventResponse;
import br.edu.ifpr.mcp_server.dto.EventSummary;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class ListarEventosTool implements McpToolDefinition {

    private final EventService eventService;

    public ListarEventosTool() {
        this(new EventService());
    }

    public ListarEventosTool(EventService eventService) {

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

                        "pagina", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "description",
                                "Número da página dos eventos. Padrão: 1."
                        ),

                        "limite", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "description",
                                "Quantidade de eventos por página. Padrão: 10."
                        ),

                        "somente_futuros", Map.of(
                                "type", "boolean",
                                "description",
                                "Quando verdadeiro, retorna apenas os próximos eventos."
                        ),

                        "possui_estatisticas", Map.of(
                                "type", "boolean",
                                "description",
                                "Filtra eventos pela disponibilidade de estatísticas."
                        )
                ),

                "additionalProperties", false
        );

        Tool tool = Tool.builder(
                "listar_eventos",
                inputSchema
        )
                .title("Listar eventos")
                .description(
                        "Lista eventos de MMA, incluindo informações "
                        + "sobre data, local, status e disponibilidade "
                        + "de estatísticas. Também permite consultar "
                        + "somente os próximos eventos."
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

            int page = integerArgument(
                    arguments,
                    "pagina",
                    1
            );

            int limit = integerArgument(
                    arguments,
                    "limite",
                    10
            );

            boolean onlyUpcoming = booleanArgument(
                    arguments,
                    "somente_futuros",
                    false
            );

            Boolean hasStats = optionalBooleanArgument(
                    arguments,
                    "possui_estatisticas"
            );

            EventResponse response;

            if (onlyUpcoming) {

                response = eventService.getUpcomingEvents();

            } else {

                response = eventService.getEvents(
                        page,
                        limit,
                        hasStats
                );
            }

            String message = formatResult(
                    response,
                    onlyUpcoming
            );

            TextContent content = TextContent.builder(message)
                    .build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return errorResult(
                    "A consulta dos eventos foi interrompida."
            );

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException ex) {

            return errorResult(ex.getMessage());

        } catch (Exception ex) {

            return errorResult(
                    "Não foi possível consultar os eventos: "
                    + ex.getMessage()
            );
        }
    }

    private int integerArgument(
            Map<String, Object> arguments,
            String parameter,
            int defaultValue) {

        Object value = arguments.get(parameter);

        if (value == null) {
            return defaultValue;
        }

        int parsedValue;

        if (value instanceof Number number) {

            double numericValue = number.doubleValue();

            if (!Double.isFinite(numericValue)
                    || numericValue != Math.rint(numericValue)
                    || numericValue < Integer.MIN_VALUE
                    || numericValue > Integer.MAX_VALUE) {

                throw new IllegalArgumentException(
                        "O parâmetro '" + parameter
                        + "' deve ser um número inteiro."
                );
            }

            parsedValue = (int) numericValue;

        } else if (value instanceof String text) {

            try {

                parsedValue = Integer.parseInt(text);

            } catch (NumberFormatException ex) {

                throw new IllegalArgumentException(
                        "O parâmetro '" + parameter
                        + "' deve ser um número inteiro."
                );
            }

        } else {

            throw new IllegalArgumentException(
                    "O parâmetro '" + parameter
                    + "' deve ser um número inteiro."
            );
        }

        if (parsedValue < 1) {
            throw new IllegalArgumentException(
                    "O parâmetro '" + parameter
                    + "' deve ser maior ou igual a 1."
            );
        }

        return parsedValue;
    }

    private Boolean optionalBooleanArgument(
            Map<String, Object> arguments,
            String parameter) {

        Object value = arguments.get(parameter);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof String text) {

            if ("true".equalsIgnoreCase(text)) {
                return true;
            }

            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }

        throw new IllegalArgumentException(
                "O parâmetro '" + parameter
                + "' deve ser verdadeiro ou falso."
        );
    }

    private boolean booleanArgument(
            Map<String, Object> arguments,
            String parameter,
            boolean defaultValue) {

        Boolean value = optionalBooleanArgument(
                arguments,
                parameter
        );

        return value == null ? defaultValue : value;
    }

    private String formatResult(
            EventResponse response,
            boolean onlyUpcoming) {

        if (response == null
                || response.data() == null
                || response.data().isEmpty()) {

            return onlyUpcoming
                    ? "Nenhum próximo evento foi encontrado."
                    : "Nenhum evento foi encontrado.";
        }

        List<EventSummary> events = response.data();

        StringBuilder result = new StringBuilder();

        result.append(
                onlyUpcoming
                        ? "PRÓXIMOS EVENTOS"
                        : "EVENTOS ENCONTRADOS"
        );

        result.append("\nTotal nesta consulta: ")
                .append(events.size());

        EventResponse.Pagination pagination =
                response.pagination();

        if (pagination != null) {

            appendField(result, "Página atual", pagination.page());
            appendField(result, "Total de eventos", pagination.total());
            appendField(result, "Total de páginas", pagination.totalPages());
        }

        for (int index = 0; index < events.size(); index++) {

            EventSummary event = events.get(index);

            if (event == null) {
                continue;
            }

            String title = event.title();

            if (title == null || title.isBlank()) {
                title = event.shortTitle();
            }

            if (title == null || title.isBlank()) {
                title = "Evento sem título";
            }

            result.append("\n\n")
                    .append(index + 1)
                    .append(". ")
                    .append(title);

            appendField(result, "ID do evento", event.id());
            appendField(result, "Identificador", event.slug());
            appendField(result, "Data", event.eventDateLabel());

            if (event.eventDateLabel() == null
                    || event.eventDateLabel().isBlank()) {

                appendField(result, "Data", event.eventDate());
            }

            appendField(result, "Horário", event.startsAt());

            appendField(
                    result,
                    "Local",
                    getLocation(event)
            );

            appendField(result, "Status", event.status());

            if (event.isLive() != null) {
                appendField(
                        result,
                        "Ao vivo",
                        event.isLive() ? "Sim" : "Não"
                );
            }

            if (event.hasStats() != null) {
                appendField(
                        result,
                        "Possui estatísticas",
                        event.hasStats() ? "Sim" : "Não"
                );
            }
        }

        return result.toString();
    }

    private String getLocation(EventSummary event) {

        if (event.locationText() != null
                && !event.locationText().isBlank()) {

            return event.locationText();
        }

        StringJoiner location = new StringJoiner(", ");

        addLocationPart(location, event.venue());
        addLocationPart(location, event.city());
        addLocationPart(location, event.state());
        addLocationPart(location, event.country());

        String formattedLocation = location.toString();

        return formattedLocation.isBlank()
                ? null
                : formattedLocation;
    }

    private void addLocationPart(
            StringJoiner location,
            String value) {

        if (value != null && !value.isBlank()) {
            location.add(value);
        }
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
                        ? "Ocorreu um erro ao consultar os eventos."
                        : message
        ).build();

        return CallToolResult.builder()
                .content(List.of(content))
                .isError(true)
                .build();
    }
}
