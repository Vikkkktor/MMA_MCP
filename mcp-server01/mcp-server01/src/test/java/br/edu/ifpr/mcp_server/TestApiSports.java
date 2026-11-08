package br.edu.ifpr.mcp_server;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.api.ApiSportsMmaClient;
import br.edu.ifpr.mcp_server.dto.FighterSummary;

public class TestApiSports {

    public static void main(String[] args) {

        String apiKey = System.getenv("API_SPORTS_MMA_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure a variável API_SPORTS_MMA_KEY."
            );
        }

        ApiSportsMmaClient client =
                new ApiSportsMmaClient(apiKey);

        String searchName = args.length > 0
                ? args[0]
                : "Conor McGregor";

        try {

            System.out.println(
                    "PESQUISANDO LUTADOR: " + searchName
            );

            List<FighterSummary> fighters =
                    client.searchFighters(searchName);

            if (fighters.isEmpty()) {

                System.out.println(
                        "Nenhum lutador encontrado."
                );

                return;
            }

            for (FighterSummary fighter : fighters) {

                System.out.println("--------------------------");

                System.out.println("ID: " + fighter.id());
                System.out.println("Nome: " + fighter.name());
                System.out.println("Apelido: " + fighter.nickname());
                System.out.println("Categoria: " + fighter.category());
                System.out.println("Equipe: " + fighter.teamName());
            }

            int fighterId = fighters.get(0).id();

            System.out.println(
                    "\nCONSULTANDO PERFIL DO LUTADOR..."
            );

            FighterSummary fighter =
                    client.getFighterById(fighterId);

            System.out.println("\nPERFIL DO LUTADOR");

            System.out.println("Nome: " + fighter.name());
            System.out.println("Apelido: " + fighter.nickname());
            System.out.println("Nascimento: " + fighter.birthDate());
            System.out.println("Idade: " + fighter.age());
            System.out.println("Altura: " + fighter.height());
            System.out.println("Peso: " + fighter.weight());
            System.out.println("Alcance: " + fighter.reach());
            System.out.println("Base: " + fighter.stance());
            System.out.println("Categoria: " + fighter.category());
            System.out.println("Equipe: " + fighter.teamName());

            System.out.println(
                    "\nCONSULTANDO RETROSPECTO DO LUTADOR..."
            );

            JsonNode records =
                    client.getFighterRecords(fighterId);

            System.out.println("\nRETROSPECTO");

            if (records.isEmpty()) {

                System.out.println(
                        "A API não disponibilizou o retrospecto."
                );

            } else {

                ObjectMapper objectMapper = new ObjectMapper();

                System.out.println(
                        objectMapper
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(records)
                );
            }

            System.out.println("\nTESTE CONCLUÍDO!");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "A requisição foi interrompida."
            );

        } catch (IOException e) {

            System.err.println(
                    "Erro na comunicação com a API-Sports: "
                    + e.getMessage()
            );
        }
    }
}