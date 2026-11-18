package br.edu.ifpr.mcp_server.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;
import br.edu.ifpr.mcp_server.dto.FighterProfile;


public class FighterService {

    private static final String API_KEY_ENVIRONMENT_VARIABLE =
            "FIGHTERAPI_MMA_KEY";

    private final FighterApiClient fighterApiClient;

    public FighterService() {

        String apiKey = System.getenv(
                API_KEY_ENVIRONMENT_VARIABLE
        );

        this.fighterApiClient =
                apiKey == null || apiKey.isBlank()
                        ? null
                        : new FighterApiClient(apiKey);
    }

    public FighterService(FighterApiClient fighterApiClient) {

        this.fighterApiClient = Objects.requireNonNull(
                fighterApiClient,
                "O cliente da FighterAPI deve ser informado."
        );
    }

    public List<FighterSearchResult> searchFighters(String name)
            throws IOException, InterruptedException {

        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Informe o nome ou parte do nome do lutador."
            );
        }

        if (fighterApiClient == null) {

            throw new IllegalStateException(
                    "A integração com a FighterAPI não está configurada. "
                    + "Defina a variável de ambiente "
                    + API_KEY_ENVIRONMENT_VARIABLE + "."
            );
        }

        return fighterApiClient.searchFighters(name.trim());
    }
    
    public FighterProfile getFighterProfile(String fighterId)
            throws IOException, InterruptedException {

        if (fighterId == null || fighterId.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o identificador do lutador."
            );
        }

        if (fighterApiClient == null) {
            throw new IllegalStateException(
                    "A integração com a FighterAPI não está configurada. "
                    + "Defina a variável de ambiente "
                    + API_KEY_ENVIRONMENT_VARIABLE + "."
            );
        }

        return fighterApiClient.getFighter(fighterId.trim());
    }
}
