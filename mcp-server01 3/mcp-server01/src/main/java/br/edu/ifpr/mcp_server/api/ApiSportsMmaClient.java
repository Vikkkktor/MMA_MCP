package br.edu.ifpr.mcp_server.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.dto.FighterSummary;

public class ApiSportsMmaClient {

    private static final String BASE_URL =
            "https://v1.mma.api-sports.io";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public ApiSportsMmaClient(String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "A chave da API-Sports não foi configurada."
            );
        }

        this.apiKey = apiKey;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public List<FighterSummary> searchFighters(String name)
            throws IOException, InterruptedException {

        // Validação do parâmetro
        if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException(
                    "O nome deve possuir pelo menos 3 caracteres."
            );
        }

        // Codificação do parâmetro para URL
        String encodedName = URLEncoder.encode(
                name.trim(),
                StandardCharsets.UTF_8
        );

        // Montagem da URL
        URI uri = URI.create(
                BASE_URL + "/fighters?search=" + encodedName
        );

        // Construção da requisição HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(20))
                .header("x-apisports-key", apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        // Execução da requisição
        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        // Verificação do status HTTP
        int statusCode = response.statusCode();

        if (statusCode == 401 || statusCode == 403) {
            throw new IOException(
                    "Autenticação inválida na API-Sports."
            );
        }

        if (statusCode == 429) {
            throw new IOException(
                    "Limite de requisições da API-Sports excedido."
            );
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException(
                    "Erro HTTP na API-Sports: " + statusCode
            );
        }

        // Conversão da resposta JSON
        JsonNode root = objectMapper.readTree(response.body());

        // Verificação de erros retornados pela API
        JsonNode errors = root.path("errors");

        if ((errors.isArray() || errors.isObject())
                && errors.size() > 0) {

            throw new IOException(
                    "Erro retornado pela API-Sports: "
                    + errors
            );
        }

        // Extração dos lutadores
        JsonNode fightersNode = root.path("response");

        if (!fightersNode.isArray()) {
            throw new IOException(
                    "Formato de resposta inesperado."
            );
        }

        List<FighterSummary> fighters = new ArrayList<>();

        for (JsonNode fighter : fightersNode) {

            FighterSummary summary = new FighterSummary(
                    fighter.path("id").asInt(-1),
                    textOrNull(fighter, "name"),
                    textOrNull(fighter, "nickname"),
                    textOrNull(fighter, "photo"),
                    textOrNull(fighter, "gender"),
                    textOrNull(fighter, "birth_date"),
                    integerOrNull(fighter, "age"),
                    textOrNull(fighter, "height"),
                    textOrNull(fighter, "weight"),
                    textOrNull(fighter, "reach"),
                    textOrNull(fighter, "stance"),
                    textOrNull(fighter, "category"),
                    textOrNull(fighter.path("team"), "name"),
                    textOrNull(fighter, "last_update")
            );

            fighters.add(summary);
        }

        return List.copyOf(fighters);
    }

    private String textOrNull(JsonNode node, String field) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()) {
            return null;
        }

        return value.asText();
    }

    private Integer integerOrNull(JsonNode node, String field) {

        JsonNode value = node.get(field);

        if (value == null || value.isNull()
                || !value.canConvertToInt()) {
            return null;
        }

        return value.asInt();
    }
}
