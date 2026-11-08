package br.edu.ifpr.mcp_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FighterSearchResult (

        @JsonProperty("fighter_id")
        String fighterId,

        String name,

        String nickname,

        @JsonProperty("weight_class")
        String weightClass,

        String country,

        @JsonProperty("date_of_birth")
        String dateOfBirth,

        @JsonProperty("pro_record")
        String proRecord,

        Integer bouts,

        @JsonProperty("last_bout")
        String lastBout,

        @JsonProperty("ufc_bouts")
        Integer ufcBouts

) {
}