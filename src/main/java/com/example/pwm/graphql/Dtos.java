package com.example.pwm.graphql;

public class Dtos {
    public record Viewer(String id) { }
    public record VaultItem(Long id, String title, String username, String password, String url, String notes, String createdAt) { }
    public record VaultUpsertInput(String title, String username, String password, String url, String notes) { }
}
