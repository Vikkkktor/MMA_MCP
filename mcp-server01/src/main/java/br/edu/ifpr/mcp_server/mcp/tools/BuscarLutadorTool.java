
package br.edu.ifpr.mcp_server.mcp.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.Service.FighterService;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;
import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;

import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;

import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class BuscarLutadorTool implements McpToolDefinition {

    private final FighterService fighterService;

    public BuscarLutadorTool() {
        this(new FighterService());
    }

    public BuscarLutadorTool(FighterService fighterService) {

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
                        "nome", Map.of(
                                "type", "string",
                                "description",
                                "Nome ou parte do nome do lutador"
                        )
                ),
                "required", List.of("nome"),
                "additionalProperties", false
        );

        Tool tool = Tool.builder("buscar_lutador", inputSchema)
                .title("Buscar lutador")
                .description(
                        "Pesquisa lutadores pelo nome ou parte do nome e "
                        + "retorna os dados básicos encontrados na FighterAPI."
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

            Object rawName = request.arguments() == null
                    ? null
                    : request.arguments().get("nome");

            if (!(rawName instanceof String name) || name.isBlank()) {

                return errorResult(
                        "Parâmetro inválido: informe o nome do lutador no campo 'nome'."
                );
            }

            List<FighterSearchResult> fighters =
                    fighterService.searchFighters(name);

            String message = formatResult(name.trim(), fighters);

            TextContent content = TextContent.builder(message)
                    .annotations(
                            Annotations.builder()
                                    .audience(List.of(Role.USER))
                                    .priority(1.0)
                                    .build()
                    )
                    .build();

            return CallToolResult.builder()
                    .content(List.of(content))
                    .build();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return errorResult(
                    "A busca de lutadores foi interrompida. Tente novamente."
            );

        } catch (
                IOException
                | IllegalArgumentException
                | IllegalStateException ex) {

            return errorResult(ex.getMessage());

        } catch (Exception ex) {

            return errorResult(
                    "Não foi possível buscar lutadores: " + ex.getMessage()
            );
        }
    }

    private String formatResult(
            String searchedName,
            List<FighterSearchResult> fighters) {

        if (fighters == null || fighters.isEmpty()) {

            return "Nenhum lutador encontrado para: " + searchedName;
        }

        StringBuilder result = new StringBuilder();

        result.append("Foram encontrados ")
                .append(fighters.size())
                .append(" lutador(es) para '")
                .append(searchedName)
                .append("':");

        for (int index = 0; index < fighters.size(); index++) {

            FighterSearchResult fighter = fighters.get(index);

            result.append("\n\n")
                    .append(index + 1)
                    .append(". ")
                    .append(valueOrUnavailable(fighter.name()));

            appendField(result, "ID", fighter.fighterId());
            appendField(result, "Apelido", fighter.nickname());
            appendField(result, "Categoria", fighter.weightClass());
            appendField(result, "País", fighter.country());
            appendField(result, "Data de nascimento", fighter.dateOfBirth());
            appendField(result, "Retrospecto profissional", fighter.proRecord());
            appendField(result, "Total de lutas", fighter.bouts());
            appendField(result, "Última luta", fighter.lastBout());
            appendField(result, "Lutas no UFC", fighter.ufcBouts());
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

    private String valueOrUnavailable(String value) {

        return value == null || value.isBlank()
                ? "Nome não informado"
                : value;
    }


    private CallToolResult errorResult(String message) {

        TextContent content = TextContent.builder(
                message == null || message.isBlank()
                        ? "Ocorreu um erro ao executar a ferramenta."
                        : message
        ).build();

        return CallToolResult.builder()
                .content(List.of(content))
                .isError(true)
                .build();
    }
}
