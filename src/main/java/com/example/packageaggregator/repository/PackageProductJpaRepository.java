package com.example.packageaggregator.repository;

import com.example.packageaggregator.domain.entity.PackageProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PackageProductJpaRepository extends JpaRepository<PackageProductEntity, UUID> {

    List<PackageProductEntity> findAllByPackageEntityId(UUID packageId);
}
