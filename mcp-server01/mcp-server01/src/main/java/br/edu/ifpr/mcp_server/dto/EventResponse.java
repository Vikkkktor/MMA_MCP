
package br.edu.ifpr.mcp_server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventResponse(

        Boolean success,

        List<EventSummary> data,

        @JsonProperty("meta")
        Pagination pagination

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(

            Integer page,
            Integer limit,
            Integer total,
            Integer totalPages,
            Boolean hasNextPage,
            Boolean hasPreviousPage,
            Integer nextPage,
            Integer previousPage

    ) {
    }
}
