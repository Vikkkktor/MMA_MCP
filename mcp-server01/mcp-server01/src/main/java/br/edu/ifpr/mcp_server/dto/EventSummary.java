
package br.edu.ifpr.mcp_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventSummary(

        String id,

        String slug,

        String title,

        String shortTitle,

        String status,

        Boolean isLive,

        String startsAt,

        String eventDate,

        String eventDateLabel,

        String venue,

        String city,

        String state,

        String country,

        String locationText,

        String imageUrl,

        Boolean hasStats,

        String freshnessStatus

) {
}
