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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.edu.ifpr.mcp_server.dto.FighterProfile;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;

public class FighterApiClient {
	
	private static final String BASE_URL = "https://mcp.fighterapi.com";
	
	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final String apiKey;
	
	public FighterApiClient(String apiKey) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalArgumentException(
					"A chave da FighterAPI não foi configurada."
			);
		}
		
		this.apiKey = apiKey;
		
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		
		this.objectMapper = new ObjectMapper ();
				
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
		public List<FighterSearchResult> searchFighters(String query) 
			throws IOException, InterruptedException {
			if (query == null || query.isBlank()) {
				throw new IllegalArgumentException("O nome do lutador nao pode ser vazio");
			}
			
			ObjectNode requestBody = objectMapper.createObjectNode();
			
			requestBody.put("query", query);
			
			JsonNode response = sendPost(
					"/v1/search_fighters",
					requestBody
					);
					
			JsonNode fightersNode = response.path("fighters");
			
			 if (!fightersNode.isArray()) {
		            throw new IOException(
		                    "Formato inesperado."
		            );
		        }
			 
		List<FighterSearchResult> fighters = new ArrayList<>();

		        for (JsonNode fighterNode : fightersNode) {

		            FighterSearchResult fighter =
		                    objectMapper.treeToValue(
		                            fighterNode,
		                            FighterSearchResult.class
		                    );

		            fighters.add(fighter);
		        }

		        return List.copyOf(fighters);
		    }
		
		public FighterProfile getFighter(String fighterId)
			throws IOException, InterruptedException{

			if (fighterId == null || fighterId.isBlank()) {
				throw new IllegalArgumentException("O Id de lutador não pode ser vazio");
			}
			
			ObjectNode requestBody = objectMapper.createObjectNode();
			
			requestBody.put("fighter_id", fighterId);
			
			JsonNode response = sendPost(
					"/v1/get_fighter",
					requestBody
					);
			
			JsonNode fighterNode = response.path("fighter");
			
			if (fighterNode.isMissingNode() || fighterNode.isNull()) {
				throw new IOException("A API não retornou o perfil de lutador");
			}
			
			return objectMapper.treeToValue(
					response,
					FighterProfile.class
					);
		}

	    private JsonNode sendPost(String endpoint, ObjectNode requestBody)
	            throws IOException, InterruptedException {

	        String jsonBody = objectMapper.writeValueAsString(requestBody);

	        URI uri = URI.create(BASE_URL + endpoint);

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(uri)
	                .timeout(Duration.ofSeconds(30))
	                .header("x-api-key", apiKey)
	                .header("Content-Type", "application/json")
	                .header("Accept", "application/json")
	                .POST(
	                    HttpRequest.BodyPublishers.ofString(
	                            jsonBody,
	                            StandardCharsets.UTF_8
	                    )
	                )
	                .build();

	        HttpResponse<String> response = httpClient.send(
	                request,
	                HttpResponse.BodyHandlers.ofString()
	        );

	        int statusCode = response.statusCode();

	        if (statusCode < 200 || statusCode >= 300) {

	            throw createApiException(
	                    statusCode,
	                    response.body()
	            );
	        }

	        JsonNode root = objectMapper.readTree(response.body());

	        if (root == null || root.isNull()) {
	            throw new IOException(
	                    "A API retornou uma resposta vazia."
	            );
	        }

	        if (root.hasNonNull("error")) {
	            throw new IOException(
	                    "Erro da FighterAPI: "
	                    + root.path("message").asText(
	                            root.path("error").asText()
	                    )
	            );
	        }

	        if (root.has("data") && root.get("data").isObject()) {
	            return root.get("data");
	        }

	        return root;
	    }

	    private IOException createApiException(
	            int statusCode,
	            String responseBody) {

	        String description = switch (statusCode) {

	            case 400 -> "Parâmetros inválidos.";

	            case 401 -> "Chave de API inválida ou ausente.";

	            case 402 -> "Créditos insuficientes.";

	            case 429 -> "Limite de requisições excedido.";

	            case 500 -> "Erro interno no servidor da FighterAPI.";

	            default -> "Falha na comunicação com a FighterAPI.";
	        };

	        String additionalInfo = "";

	        try {

	            JsonNode error = objectMapper.readTree(responseBody);

	            if (error != null) {

	                additionalInfo = error.path("message")
	                        .asText("");
	            }

	        } catch (IOException ignored) {

	        }

	        return new IOException(
	                description
	                + " HTTP: " + statusCode
	                + (additionalInfo.isBlank()
	                        ? ""
	                        : " Detalhes: " + additionalInfo)
	        );
	    }
	}


        
		

	
	







