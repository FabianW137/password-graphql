package com.example.pwm.graphql.db;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VaultItemRepository extends ReactiveCrudRepository<VaultItemEntity, Long> {
    Flux<VaultItemEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    Mono<VaultItemEntity> findByIdAndOwnerId(Long id, UUID ownerId);
}
