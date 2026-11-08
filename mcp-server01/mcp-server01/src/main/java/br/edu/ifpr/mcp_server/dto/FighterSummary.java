package br.edu.ifpr.mcp_server.dto;

public record FighterSummary(
        int id,
        String name,
        String nickname,
        String photo,
        String gender,
        String birthDate,
        Integer age,
        String height,
        String weight,
        String reach,
        String stance,
        String category,
        String teamName,
        String lastUpdate
) {
}