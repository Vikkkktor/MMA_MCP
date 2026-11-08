
package br.edu.ifpr.mcp_server;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpr.mcp_server.api.CitoApiClient;
import br.edu.ifpr.mcp_server.dto.EventResponse;
import br.edu.ifpr.mcp_server.dto.EventSummary;

public class TestCitoApi {

    public static void main(String[] args) {

        String apiKey = System.getenv("CITOAPI_MMA_KEY");

        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "Configure a variável CITOAPI_MMA_KEY."
            );
        }

        CitoApiClient client = new CitoApiClient(apiKey);

        ObjectMapper mapper = new ObjectMapper();

        try {

            System.out.println("VERIFICANDO CONEXÃO COM A CITO...");

            JsonNode health = client.getHealth();

            System.out.println("\nSTATUS DA API:");

            System.out.println(health.toPrettyString());

            JsonNode keyStatus = health.path("key");

            if (keyStatus.has("valid")
                    && !keyStatus.path("valid").asBoolean()) {

                throw new IOException(
                        "A chave da Cito API é inválida."
                );
            }

            System.out.println("\nCONSULTANDO PRÓXIMOS EVENTOS...");

            JsonNode upcomingJson = client.getUpcomingEvents();

            EventResponse upcomingResponse =
                    mapper.treeToValue(
                            upcomingJson,
                            EventResponse.class
                    );

            System.out.println("\nPRÓXIMOS EVENTOS");

            System.out.println(
                    "Consulta realizada com sucesso: "
                    + upcomingResponse.success()
            );

            if (upcomingResponse.data() != null) {

                for (EventSummary event : upcomingResponse.data()) {

                    System.out.println("\n----------------------------");

                    System.out.println("Nome: " + event.title());

                    System.out.println("ID: " + event.id());

                    System.out.println("Slug: " + event.slug());

                    System.out.println("Data: " + event.eventDate());

                    System.out.println("Horário: " + event.startsAt());

                    System.out.println("Local: " + event.locationText());

                    System.out.println("Status: " + event.status());

                    System.out.println("Ao vivo: " + event.isLive());

                    System.out.println(
                            "Possui estatísticas: " + event.hasStats()
                    );
                }

            } else {

                System.out.println("Nenhum evento encontrado.");
            }

            System.out.println("\nCONSULTANDO EVENTOS PAGINADOS...");

            JsonNode eventsJson = client.getEvents(1, 10, null);

            EventResponse eventsResponse =
                    mapper.treeToValue(
                            eventsJson,
                            EventResponse.class
                    );

            System.out.println("\nINFORMAÇÕES DA PAGINAÇÃO");

            EventResponse.Pagination pagination =
                    eventsResponse.pagination();

            if (pagination != null) {

                System.out.println(
                        "Página atual: " + pagination.page()
                );

                System.out.println(
                        "Eventos por página: " + pagination.limit()
                );

                System.out.println(
                        "Total de eventos: " + pagination.total()
                );

                System.out.println(
                        "Total de páginas: " + pagination.totalPages()
                );

                System.out.println(
                        "Existe próxima página: "
                        + pagination.hasNextPage()
                );

                System.out.println(
                        "Próxima página: " + pagination.nextPage()
                );

            } else {

                System.out.println(
                        "A resposta não contém informações de paginação."
                );
            }

            System.out.println("\nTESTES CONCLUÍDOS!");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "A requisição foi interrompida."
            );

        } catch (IOException e) {

            System.err.println(
                    "Erro na comunicação com a Cito API: "
                    + e.getMessage()
            );
        }
    }
}
