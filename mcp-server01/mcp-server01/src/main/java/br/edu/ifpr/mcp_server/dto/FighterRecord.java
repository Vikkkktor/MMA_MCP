
package br.edu.ifpr.mcp_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FighterRecord(

        FighterReference fighter,

        RecordBreakdown total,

        RecordBreakdown ko,

        RecordBreakdown sub

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FighterReference(

            Integer id,

            String name,

            String photo

    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecordBreakdown(

            Integer win,

            Integer loss,

            Integer draw

    ) {
    }
}
