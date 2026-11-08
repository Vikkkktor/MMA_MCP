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
import java.util.Map;
import java.util.StringJoiner;

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

        if (name == null || name.trim().length() < 3) {
            throw new IllegalArgumentException(
                    "O nome deve possuir pelo menos 3 caracteres."
            );
        }

        JsonNode root = sendGet(
                "/fighters",
                Map.of("search", name.trim())
        );

        return parseFighters(root);
    }

    public FighterSummary getFighterById(int fighterId)
            throws IOException, InterruptedException {

        validateFighterId(fighterId);

        JsonNode root = sendGet(
                "/fighters",
                Map.of("id", String.valueOf(fighterId))
        );

        List<FighterSummary> fighters = parseFighters(root);

        if (fighters.isEmpty()) {
            throw new IOException(
                    "Nenhum lutador encontrado com o ID " + fighterId + "."
            );
        }

        return fighters.get(0);
    }

    public JsonNode getFighterRecords(int fighterId)
            throws IOException, InterruptedException {

        validateFighterId(fighterId);

        JsonNode root = sendGet(
                "/fighters/records",
                Map.of("id", String.valueOf(fighterId))
        );

        JsonNode records = root.path("response");

        if (records.isMissingNode() || records.isNull()) {
            throw new IOException(
                    "A API não retornou os dados do retrospecto."
            );
        }

        return records;
    }

    private List<FighterSummary> parseFighters(JsonNode root)
            throws IOException {

        JsonNode response = root.path("response");

        if (!response.isArray()) {
            throw new IOException(
                    "Formato inesperado na resposta de lutadores."
            );
        }

        List<FighterSummary> fighters = new ArrayList<>();

        for (JsonNode fighter : response) {

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

    private JsonNode sendGet(
            String endpoint,
            Map<String, String> parameters
    ) throws IOException, InterruptedException {

        URI uri = buildUri(endpoint, parameters);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(20))
                .header("x-apisports-key", apiKey)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        int status = response.statusCode();

        if (status == 401 || status == 403) {
            throw new IOException(
                    "Autenticação inválida na API-Sports. HTTP: " + status
            );
        }

        if (status == 429) {
            throw new IOException(
                    "Limite de requisições da API-Sports excedido."
            );
        }

        if (status < 200 || status >= 300) {
            throw new IOException(
                    "Erro HTTP na API-Sports: "
                    + status
                    + ". Resposta: "
                    + response.body()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        if (root == null || root.isNull()) {
            throw new IOException(
                    "A API-Sports retornou uma resposta vazia."
            );
        }

        JsonNode errors = root.path("errors");

        if (!errors.isMissingNode()
                && !errors.isNull()
                && errors.size() > 0) {

            throw new IOException(
                    "Erro retornado pela API-Sports: " + errors
            );
        }

        return root;
    }

    private URI buildUri(
            String endpoint,
            Map<String, String> parameters
    ) {

        StringBuilder url = new StringBuilder(BASE_URL)
                .append(endpoint);

        if (!parameters.isEmpty()) {

            StringJoiner query = new StringJoiner("&");

            for (Map.Entry<String, String> parameter
                    : parameters.entrySet()) {

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

            url.append('?').append(query);
        }

        return URI.create(url.toString());
    }


    private void validateFighterId(int fighterId) {

        if (fighterId <= 0) {
            throw new IllegalArgumentException(
                    "O ID do lutador deve ser maior que zero."
            );
        }
    }

    private String textOrNull(JsonNode node, String field) {

        JsonNode value = node.get(field);

        return value == null || value.isNull()
                ? null
                : value.asText();
    }

    private Integer integerOrNull(JsonNode node, String field) {

        JsonNode value = node.get(field);

        return value == null
                || value.isNull()
                || !value.canConvertToInt()
                        ? null
                        : value.asInt();
    }
}