package com.example.pwm.graphql.service;

import com.example.pwm.graphql.Dtos;
import com.example.pwm.graphql.crypto.CryptoService;
import com.example.pwm.graphql.db.VaultItemEntity;
import com.example.pwm.graphql.db.VaultItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VaultService {

    private final VaultItemRepository repo;
    private final CryptoService crypto;

    public VaultService(VaultItemRepository repo, CryptoService crypto) {
        this.repo = repo;
        this.crypto = crypto;
    }

    public Flux<Dtos.VaultItem> list(UUID ownerId) {
        return repo.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
                .map(this::toDto);
    }

    public Mono<Dtos.VaultItem> get(UUID ownerId, Long id) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder falscher Owner")))
                .map(this::toDto);
    }

    public Mono<Dtos.VaultItem> create(UUID ownerId, Dtos.VaultUpsertEncInput in) {
        var e = new VaultItemEntity();
        e.setOwnerId(ownerId);
        e.setTitleEnc(   crypto.ensureEncrypted(ownerId, in.titleEnc()));
        e.setUsernameEnc(crypto.ensureEncrypted(ownerId, in.usernameEnc()));
        e.setPasswordEnc(crypto.ensureEncrypted(ownerId, in.passwordEnc()));
        e.setUrlEnc(     crypto.ensureEncrypted(ownerId, in.urlEnc()));
        e.setNotesEnc(   crypto.ensureEncrypted(ownerId, in.notesEnc()));

        // >>> FIX: Timestamps setzen (verhindert "temporal"/Null-Fehler)
        Instant now = Instant.now();
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return repo.save(e).map(this::toDto);
    }

    public Mono<Dtos.VaultItem> update(UUID ownerId, Long id, Dtos.VaultUpsertEncInput in) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder falscher Owner")))
                .flatMap(e -> {
                    if (in.titleEnc()    != null) e.setTitleEnc(   crypto.ensureEncrypted(ownerId, in.titleEnc()));
                    if (in.usernameEnc() != null) e.setUsernameEnc(crypto.ensureEncrypted(ownerId, in.usernameEnc()));
                    if (in.passwordEnc() != null) e.setPasswordEnc(crypto.ensureEncrypted(ownerId, in.passwordEnc()));
                    if (in.urlEnc()      != null) e.setUrlEnc(     crypto.ensureEncrypted(ownerId, in.urlEnc()));
                    if (in.notesEnc()    != null) e.setNotesEnc(   crypto.ensureEncrypted(ownerId, in.notesEnc()));

                    // >>> FIX: updatedAt immer aktualisieren
                    e.setUpdatedAt(Instant.now());

                    return repo.save(e);
                })
                .map(this::toDto);
    }

    public Mono<Boolean> delete(UUID ownerId, Long id) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder falscher Owner")))
                .flatMap(e -> repo.deleteById(e.getId()).thenReturn(true));
    }

    // --- Helper ---

    private static String iso(Instant t) {
        // NIE versuchen, ein null-Instant zu formatieren
        return (t == null) ? DateTimeFormatter.ISO_INSTANT.format(Instant.EPOCH)
                : DateTimeFormatter.ISO_INSTANT.format(t);
    }

    private Dtos.VaultItem toDto(VaultItemEntity e) {
        return new Dtos.VaultItem(
                e.getId(),
                e.getOwnerId() != null ? e.getOwnerId().toString() : null,
                e.getTitleEnc(),
                e.getUsernameEnc(),
                e.getPasswordEnc(),
                e.getUrlEnc(),
                e.getNotesEnc(),
                iso(e.getCreatedAt()),
                iso(e.getUpdatedAt())
        );
    }
}
