package br.edu.ifpr.mcp_server.mcp.tools;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.edu.ifpr.mcp_server.mcp.utils.McpToolDefinition;
import io.modelcontextprotocol.server.McpServer.SingleSessionSyncSpecification;
import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Annotations;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class SimpleReportTool implements McpToolDefinition {
//   private static final Logger LOG = LoggerFactory.getLogger(SimpleReportTool.class);

   public SimpleReportTool() {}

   @Override
   public void register(SyncSpecification<SingleSessionSyncSpecification> mcp)
               throws Exception {
      // define o esquema para troca de dados
      Map<String, Object> inputSchema =
    			          Map.of("type","object",
    			                 "properties",Map.of("id",
    			                			         Map.of("type","integer",
                                                            "description",
                                                               "ID do documento"
    			                            			   )
    			                                    ),
                                 "required",List.of("id"),
                                 "additionalProperties",false
    			                );
      // define a ferramenta
      Tool tool = Tool.builder("obter_documento",inputSchema)
    			      .title("Obter documento JSON")
    			      .description("Retorna os dados de um documento JSON")
    			      .build();
      // vincula o objeto ferramenta (acima) à implementação da ferramenta
      SyncToolSpecification toolSpec =
                            SyncToolSpecification.builder()
                                                 .tool(tool)
                                                 // quando a ferramenta for
                                                 // selecionada, seu método
                                                 // "run" (abaixo) será
                                                 // invocado para realizar a
                                                 // tarefa programada
                                                 .callHandler(this::run)
                                                 .build();
      // acrescenta a ferramenta ao servidor
      mcp.tools(toolSpec);
   }

   @Override
   public CallToolResult run(McpSyncServerExchange exchange,
                             CallToolRequest request) {
      try {
         List<Role>  roles       = List.of(McpSchema.Role.USER);
         Annotations annotations = Annotations.builder()
        			                          .audience(roles)
        			                          .priority(1.0)
        			                          .build();
         // recupera o(s) parâmetro(s) passados pelo cliente, se houver algum
         //
         // OBSERVAÇÃO:
         // números vindos de JSON podem ser representados por diferentes
         // implementações de java.lang.Number
         //
         int    id  = ((Number) request.arguments().get("id")).intValue();
         // realiza alguma operação, com ou sem, eventuais, parâmetros do
         // cliente
         String msg = String.format("id[%d], titulo['Relatório de " +
                                    "Documentos'], status['finalizado'], " +
                                    "data[%s]",
                                    id,LocalDateTime.now().toString());
         // prepara o texto de retorno ao cliente
         TextContent content = TextContent.builder(msg)
        			                      .annotations(annotations)
        			                      .build();
         // prepara uma notificação ao cliente (opcional)
         LoggingMessageNotification lmn =
              LoggingMessageNotification.builder(LoggingLevel.INFO,
                                                 String.valueOf(id))
                                        .logger(this.getClass().getSimpleName())
                                        .build();
         // envia uma notificação ao cliente
         exchange.loggingNotification(lmn);
         // retorna o resultado da ferramenta ('tool') ao cliente
         return CallToolResult.builder()
                              .content(List.of(content))
                              .build();
      }
      catch (Exception ex) {
         throw new RuntimeException("ERRO EXECUTANDO FERRAMENTA:\n" +
                                    ex.getMessage());
      }
   }
}
