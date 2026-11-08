
package br.edu.ifpr.mcp_server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FighterProfile(

        FighterBio fighter,

        FighterRecord record,

        @JsonProperty("ufc_record")
        FighterRecord ufcRecord,

        @JsonProperty("career_rates")
        CareerRates careerRates,

        FighterRating rating,

        @JsonProperty("recent_fights")
        List<RecentFight> recentFights

) {

    public record FighterBio(

            @JsonProperty("fighter_id")
            String fighterId,

            String name,
            String nickname,

            @JsonProperty("date_of_birth")
            String dateOfBirth,

            @JsonProperty("height_in")
            Integer heightIn,

            @JsonProperty("reach_in")
            Integer reachIn,

            @JsonProperty("weight_lbs")
            Integer weightLbs,

            String stance,
            String country,
            String gym,
            String gender,

            @JsonProperty("weight_class")
            String weightClass,

            @JsonProperty("pro_record")
            String proRecord

    ) {
    }

    public record FighterRecord(

            Integer wins,
            Integer losses,
            Integer draws,

            @JsonProperty("no_contests")
            Integer noContests,

            @JsonProperty("wins_ko")
            Integer winsKo,

            @JsonProperty("wins_sub")
            Integer winsSubmission,

            @JsonProperty("wins_dec")
            Integer winsDecision,

            @JsonProperty("losses_ko")
            Integer lossesKo,

            @JsonProperty("losses_sub")
            Integer lossesSubmission,

            @JsonProperty("losses_dec")
            Integer lossesDecision

    ) {
    }

    public record CareerRates(

            @JsonProperty("minutes_fought")
            Integer minutesFought,

            @JsonProperty("sig_landed_per_15")
            Double significantStrikesLandedPer15,

            @JsonProperty("sig_absorbed_per_15")
            Double significantStrikesAbsorbedPer15,

            @JsonProperty("sig_accuracy")
            Double significantStrikesAccuracy,

            @JsonProperty("sig_defense")
            Double significantStrikesDefense,

            @JsonProperty("td_per_15")
            Double takedownsPer15,

            @JsonProperty("td_accuracy")
            Double takedownAccuracy,

            @JsonProperty("td_defense")
            Double takedownDefense,

            @JsonProperty("sub_attempts_per_15")
            Double submissionAttemptsPer15,

            @JsonProperty("control_share")
            Double controlShare,

            Integer knockdowns

    ) {
    }

    public record FighterRating(
            Integer current,
            Integer peak
    ) {
    }

    public record RecentFight(

            @JsonProperty("bout_id")
            String boutId,

            @JsonProperty("event_id")
            String eventId,

            @JsonProperty("event_name")
            String eventName,

            @JsonProperty("event_date")
            String eventDate,

            @JsonProperty("weight_class")
            String weightClass,

            String winner,
            String method,

            @JsonProperty("method_display")
            String methodDisplay,

            @JsonProperty("end_round")
            Integer endRound,

            @JsonProperty("end_clock")
            String endClock

    ) {
    }
}
