package com.example.pwm.graphql.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("vault_items")
public class VaultItemEntity {

    @Id
    private Long id;

    @Column("owner_id")
    private UUID ownerId;

    @Column("title_enc")
    private String titleEnc;

    @Column("username_enc")
    private String usernameEnc;

    @Column("password_enc")
    private String passwordEnc;

    @Column("url_enc")
    private String urlEnc;

    @Column("notes_enc")
    private String notesEnc;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getTitleEnc() { return titleEnc; }
    public void setTitleEnc(String titleEnc) { this.titleEnc = titleEnc; }

    public String getUsernameEnc() { return usernameEnc; }
    public void setUsernameEnc(String usernameEnc) { this.usernameEnc = usernameEnc; }

    public String getPasswordEnc() { return passwordEnc; }
    public void setPasswordEnc(String passwordEnc) { this.passwordEnc = passwordEnc; }

    public String getUrlEnc() { return urlEnc; }
    public void setUrlEnc(String urlEnc) { this.urlEnc = urlEnc; }

    public String getNotesEnc() { return notesEnc; }
    public void setNotesEnc(String notesEnc) { this.notesEnc = notesEnc; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
