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
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public VaultService(VaultItemRepository repo) {
        this.repo = repo;
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
        e.setTitleEnc(nn(in.titleEnc()));
        e.setUsernameEnc(nn(in.usernameEnc()));
        e.setPasswordEnc(nn(in.passwordEnc()));
        e.setUrlEnc(nn(in.urlEnc()));
        e.setNotesEnc(nn(in.notesEnc()));
        // created_at/updated_at kommen per DB-Defaults/Trigger
        return repo.save(e).map(this::toDto);
    }

    public Mono<Dtos.VaultItem> update(UUID ownerId, Long id, Dtos.VaultUpsertEncInput in) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder nicht Eigentümer")))
                .flatMap(e -> {
                    if (in.titleEnc()    != null) e.setTitleEnc(nn(in.titleEnc()));
                    if (in.usernameEnc() != null) e.setUsernameEnc(nn(in.usernameEnc()));
                    if (in.passwordEnc() != null) e.setPasswordEnc(nn(in.passwordEnc()));
                    if (in.urlEnc()      != null) e.setUrlEnc(nn(in.urlEnc()));
                    if (in.notesEnc()    != null) e.setNotesEnc(nn(in.notesEnc()));
                    return repo.save(e);
                })
                .map(this::toDto);
    }

    public Mono<Boolean> delete(UUID ownerId, Long id) {
        return repo.findByIdAndOwnerId(id, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("VaultItem nicht gefunden oder nicht Eigentümer")))
                .flatMap(e -> repo.deleteById(e.getId()).thenReturn(true));
    }
}
