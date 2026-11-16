package br.edu.ifpr.mcp_server.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.ifpr.mcp_server.api.CitoApiClient;
import br.edu.ifpr.mcp_server.dto.EventResponse;

public class EventService {

    private static final String API_KEY_ENVIRONMENT_VARIABLE =
            "CITOAPI_MMA_KEY";

    private final CitoApiClient citoApiClient;
    private final ObjectMapper objectMapper;

    public EventService() {

        String apiKey = System.getenv(
                API_KEY_ENVIRONMENT_VARIABLE
        );

        this.citoApiClient =
                apiKey == null || apiKey.isBlank()
                        ? null
                        : new CitoApiClient(apiKey);

        this.objectMapper = new ObjectMapper();
    }

    public EventService(CitoApiClient citoApiClient) {

        this.citoApiClient = Objects.requireNonNull(
                citoApiClient,
                "O cliente da Cito API deve ser informado."
        );

        this.objectMapper = new ObjectMapper();
    }

    public EventResponse getEvents(
            int page,
            int limit,
            Boolean hasStats)
            throws IOException, InterruptedException {

        if (page < 1) {
            throw new IllegalArgumentException(
                    "A página deve ser maior ou igual a 1."
            );
        }

        if (limit < 1) {
            throw new IllegalArgumentException(
                    "O limite deve ser maior ou igual a 1."
            );
        }

        validateApiConfiguration();

        JsonNode response = citoApiClient.getEvents(
                page,
                limit,
                hasStats
        );

        return convertResponse(response);
    }

    public EventResponse getUpcomingEvents()
            throws IOException, InterruptedException {

        validateApiConfiguration();

        JsonNode response = citoApiClient.getUpcomingEvents();

        return convertResponse(response);
    }

    private void validateApiConfiguration() {

        if (citoApiClient == null) {
            throw new IllegalStateException(
                    "A integração com a Cito API não está configurada. "
                    + "Defina a variável de ambiente "
                    + API_KEY_ENVIRONMENT_VARIABLE + "."
            );
        }
    }
    
    public JsonNode getEventBouts(String eventIdOrSlug)
          throws IOException, InterruptedException {

      if (eventIdOrSlug == null || eventIdOrSlug.isBlank()) {
          throw new IllegalArgumentException(
                  "Informe o ID ou identificador do evento."
          );
      }

      validateApiConfiguration();

      return citoApiClient.getEventBouts(eventIdOrSlug.trim());
  }

    private EventResponse convertResponse(JsonNode json)
            throws IOException {

        if (json == null || json.isNull()) {
            throw new IOException(
                    "A Cito API não retornou dados dos eventos."
            );
        }

        EventResponse response = objectMapper.treeToValue(
                json,
                EventResponse.class
        );

        if (response == null) {
            throw new IOException(
                    "Não foi possível interpretar os eventos retornados."
            );
        }

        if (Boolean.FALSE.equals(response.success())) {
            throw new IOException(
                    "A Cito API não conseguiu consultar os eventos."
            );
        }

        if (response.data() == null) {
            return new EventResponse(
                    response.success(),
                    List.of(),
                    response.pagination()
            );
        }

        return response;
    }
}
