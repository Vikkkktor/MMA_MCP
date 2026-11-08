
package br.edu.ifpr.mcp_server.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CitoApiClient {

    private static final String BASE_URL =
            "https://api.citoapi.com/api/v1";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;


    public CitoApiClient(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "A chave da Cito API não foi configurada."
            );
        }

        this.apiKey = apiKey;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
    }


    public JsonNode getHealth()
            throws IOException, InterruptedException {

        return sendGet("/health", Map.of());
    }

    public JsonNode getUpcomingEvents()
            throws IOException, InterruptedException {

        return sendGet(
                "/ufc/events/upcoming",
                Map.of()
        );
    }

    public JsonNode getEvents(
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

        Map<String, String> parameters = new LinkedHashMap<>();

        parameters.put("page", String.valueOf(page));
        parameters.put("limit", String.valueOf(limit));

        if (hasStats != null) {
            parameters.put(
                    "hasStats",
                    String.valueOf(hasStats)
            );
        }

        return sendGet("/ufc/events", parameters);
    }


    private JsonNode sendGet(
            String endpoint,
            Map<String, String> queryParameters)
            throws IOException, InterruptedException {

        URI uri = buildUri(endpoint, queryParameters);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(30))
                .header("x-api-key", apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        int statusCode = response.statusCode();

        if (statusCode < 200 || statusCode >= 300) {

            String retryAfter = response.headers()
                    .firstValue("Retry-After")
                    .orElse(null);

            throw createApiException(
                    statusCode,
                    response.body(),
                    retryAfter
            );
        }

        if (response.body() == null || response.body().isBlank()) {
            throw new IOException(
                    "A Cito API retornou uma resposta vazia."
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (root == null || root.isNull()) {
            throw new IOException(
                    "Não foi possível interpretar a resposta JSON."
            );
        }

        if (root.path("success").isBoolean()
                && !root.path("success").asBoolean()) {

            throw createApiException(
                    statusCode,
                    response.body(),
                    null
            );
        }

        return root;
    }

    private URI buildUri(
            String endpoint,
            Map<String, String> queryParameters) {

        StringBuilder url = new StringBuilder(BASE_URL);
        url.append(endpoint);

        if (!queryParameters.isEmpty()) {

            StringJoiner query = new StringJoiner("&");

            for (Map.Entry<String, String> parameter
                    : queryParameters.entrySet()) {

                String key = URLEncoder.encode(
                        parameter.getKey(),
                        StandardCharsets.UTF_8
                );

                String value = URLEncoder.encode(
                        parameter.getValue(),
                        StandardCharsets.UTF_8
                );

                query.add(key + "=" + value);
            }

            url.append("?").append(query);
        }

        return URI.create(url.toString());
    }


		private IOException createApiException(
		        int statusCode,
		        String responseBody,
		        String retryAfter) {
		
		    String errorCode = null;
		    String message = null;
		
		    try {
		
		        JsonNode root = objectMapper.readTree(responseBody);
		
		        JsonNode error = root.path("error");
		
		        errorCode = error.path("code").asText(null);
		
		        message = error.path("message").asText(null);
		
		        if (message == null || message.isBlank()) {
		            message = root.path("message").asText(null);
		        }
		
		    } catch (IOException ignored) {
		
		    }
		
		    if (message == null || message.isBlank()) {
		
		        message = switch (statusCode) {
		
		            case 400 -> "Requisição malformada.";
		            case 401 -> "Chave de autenticação inválida ou ausente.";
		            case 403 -> "Acesso não permitido.";
		            case 404 -> "Recurso não encontrado.";
		            case 405 -> "Método HTTP não permitido.";
		            case 422 -> "Parâmetros inválidos.";
		            case 429 -> "Limite de requisições excedido.";
		            case 500 -> "Erro interno da Cito API.";
		            case 503 -> "Serviço temporariamente indisponível.";
		
		            default -> "Erro na comunicação com a Cito API.";
		        };
		    }
		
		    StringBuilder errorMessage = new StringBuilder();
		
		    errorMessage.append(message);
		    errorMessage.append(" HTTP: ").append(statusCode);
		
		    if (errorCode != null) {
		        errorMessage.append(" Código: ").append(errorCode);
		    }
		
		    if (retryAfter != null) {
		        errorMessage.append(" Tente novamente após ")
		                .append(retryAfter)
		                .append(" segundos.");
		    }
		
		    if (responseBody != null && !responseBody.isBlank()) {
		        errorMessage.append(" Resposta: ").append(responseBody);
		    }
		
		    return new IOException(errorMessage.toString());
		}
   }

