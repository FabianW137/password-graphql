package com.example.pwm.graphql;

public class Dtos {
    public record Viewer(String id) {}

    public record VaultItem(
            Long id,
            String ownerId,
            String titleEnc,
            String usernameEnc,
            String passwordEnc,
            String urlEnc,
            String notesEnc,
            String createdAt,
            String updatedAt
    ) {}

    public record VaultUpsertEncInput(
            String titleEnc,
            String usernameEnc,
            String passwordEnc,
            String urlEnc,
            String notesEnc
    ) {}
}
