
package br.edu.ifpr.mcp_server;

import java.io.IOException;
import java.util.List;

import br.edu.ifpr.mcp_server.api.FighterApiClient;
import br.edu.ifpr.mcp_server.dto.FighterProfile;
import br.edu.ifpr.mcp_server.dto.FighterSearchResult;

public class TestFighterApi {

    public static void main(String[] args) {

        String apiKey = System.getenv("FIGHTERAPI_MMA_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure a variável FIGHTERAPI_MMA_KEY."
            );
        }

        FighterApiClient client =
                new FighterApiClient(apiKey);

        try {

            System.out.println("PESQUISANDO LUTADORES...");
            
            List<FighterSearchResult> fighters =
                    client.searchFighters("Charles");

            if (fighters.isEmpty()) {

                System.out.println(
                        "Nenhum lutador encontrado."
                );

                return;
            }

            for (FighterSearchResult fighter : fighters) {

                System.out.println("----------------------------");
                System.out.println("ID: " + fighter.fighterId());
                System.out.println("Nome: " + fighter.name());
                System.out.println("Apelido: " + fighter.nickname());
                System.out.println("Categoria: " + fighter.weightClass());
                System.out.println("País: " + fighter.country());
                System.out.println("Retrospecto: " + fighter.proRecord());
            }

            FighterSearchResult selectedFighter = fighters.get(0);

            System.out.println("\nCONSULTANDO PERFIL COMPLETO...");

            FighterProfile profile =
                    client.getFighter(selectedFighter.fighterId());

            if (profile.fighter() != null) {

                System.out.println("\nDADOS DO LUTADOR");

                System.out.println(
                        "Nome: " + profile.fighter().name()
                );

                System.out.println(
                        "Altura: " + profile.fighter().heightIn() + " polegadas"
                );

                System.out.println(
                        "Alcance: " + profile.fighter().reachIn() + " polegadas"
                );

                System.out.println(
                        "Categoria: " + profile.fighter().weightClass()
                );

                System.out.println(
                        "Equipe: " + profile.fighter().gym()
                );
            }


            if (profile.record() != null) {

                System.out.println("\nRETROSPECTO PROFISSIONAL");

                System.out.println(
                        "Vitórias: " + profile.record().wins()
                );

                System.out.println(
                        "Derrotas: " + profile.record().losses()
                );

                System.out.println(
                        "Empates: " + profile.record().draws()
                );
            }


            if (profile.rating() != null) {

                System.out.println("\nRATING ELO");

                System.out.println(
                        "Atual: " + profile.rating().current()
                );

                System.out.println(
                        "Pico: " + profile.rating().peak()
                );
            }

            if (profile.careerRates() != null) {

                System.out.println("\nESTATÍSTICAS");

                System.out.println(
                        "Golpes significativos por 15 min: "
                        + profile.careerRates()
                                .significantStrikesLandedPer15()
                );

                System.out.println(
                        "Quedas por 15 min: "
                        + profile.careerRates().takedownsPer15()
                );

                System.out.println(
                        "Precisão de quedas: "
                        + profile.careerRates().takedownAccuracy()
                );
            }

            if (profile.recentFights() != null) {

                System.out.println("\nLUTAS RECENTES");

                profile.recentFights().forEach(fight -> {

                    System.out.println("----------------------------");

                    System.out.println(
                            "Evento: " + fight.eventName()
                    );

                    System.out.println(
                            "Data: " + fight.eventDate()
                    );

                    System.out.println(
                            "Resultado: " + fight.methodDisplay()
                    );

                    System.out.println(
                            "Round final: " + fight.endRound()
                    );
                });
            }

            System.out.println("\nTESTE CONCLUÍDO!");

        } catch (IOException e) {

            System.err.println(
                    "Erro na comunicação com a FighterAPI: "
                    + e.getMessage()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "A requisição foi interrompida."
            );
        }
    }
}
