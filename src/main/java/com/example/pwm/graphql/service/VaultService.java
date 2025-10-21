package com.example.pwm.graphql.service;

import com.example.pwm.graphql.Dtos;
import com.example.pwm.graphql.db.VaultItemEntity;
import com.example.pwm.graphql.db.VaultItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class VaultService {

    private final VaultItemRepository repo;
    private final CryptoService crypto;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public VaultService(VaultItemRepository repo, CryptoService crypto) {
        this.repo = repo;
        this.crypto = crypto;
    }

    private static String nn(String s) { return (s == null) ? "" : s; }

    private Dtos.VaultItem toDto(VaultItemEntity e) {
        return new Dtos.VaultItem(
                e.getId(),
                e.getOwnerId().toString(),
                nn(e.getTitleEnc()),
                nn(e.getUsernameEnc()),
                nn(e.getPasswordEnc()),
                nn(e.getUrlEnc()),
                nn(e.getNotesEnc()),
                ISO.format(e.getCreatedAt()),
                ISO.format(e.getUpdatedAt())
        );
    }

    public Flux<Dtos.VaultItem> listByOwner(UUID ownerId) {
        return repo.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).map(this::toDto);
    }

    public Mono<Dtos.VaultItem> getById(UUID ownerId, Long id) {
        return repo.findByIdAndOwnerId(id, ownerId).map(this::toDto);
    }

    public Mono<Dtos.VaultItem> create(UUID ownerId, Dtos.VaultUpsertEncInput in) {
        VaultItemEntity e = new VaultItemEntity();
        e.setOwnerId(ownerId);

        // <<< WICHTIG: immer verschlüsseln (oder verschlüsselt belassen) >>>
        e.setTitleEnc(   crypto.ensureEncrypted(ownerId, in.titleEnc()));
        e.setUsernameEnc(crypto.ensureEncrypted(ownerId, in.usernameEnc()));
        e.setPasswordEnc(crypto.ensureEncrypted(ownerId, in.passwordEnc()));
        e.setUrlEnc(     crypto.ensureEncrypted(ownerId, in.urlEnc()));
        e.setNotesEnc(   crypto.ensureEncrypted(ownerId, in.notesEnc()));

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
                    return repo.save(e);
                })
                .map(this::toDto);
    }

    public Mono<Boolean> delete(UUID ownerId, Long id) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder falscher Owner")))
                .flatMap(e -> repo.deleteById(e.getId()).thenReturn(true));
    }
}
