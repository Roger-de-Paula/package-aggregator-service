package com.example.packageaggregator.repository;

import com.example.packageaggregator.domain.entity.PackageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PackageJpaRepository extends JpaRepository<PackageEntity, UUID> {

    Page<PackageEntity> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT p FROM PackageEntity p LEFT JOIN FETCH p.products WHERE p.id = :id AND p.deleted = false")
    Optional<PackageEntity> findByIdAndDeletedFalse(@Param("id") UUID id);
}
