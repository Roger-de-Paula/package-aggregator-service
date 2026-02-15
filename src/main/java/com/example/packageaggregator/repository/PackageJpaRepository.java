package com.example.packageaggregator.repository;

import com.example.packageaggregator.domain.entity.PackageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackageJpaRepository extends JpaRepository<PackageEntity, UUID> {

    Page<PackageEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PackageEntity> findByIdAndDeletedFalse(UUID id);
}
