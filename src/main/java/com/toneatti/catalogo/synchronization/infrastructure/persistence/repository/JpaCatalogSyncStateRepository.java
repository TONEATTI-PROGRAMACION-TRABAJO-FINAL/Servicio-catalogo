package com.toneatti.catalogo.synchronization.infrastructure.persistence.repository;

import com.toneatti.catalogo.synchronization.infrastructure.persistence.entity.CatalogSyncStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaCatalogSyncStateRepository extends JpaRepository<CatalogSyncStateEntity, Long> {
}
