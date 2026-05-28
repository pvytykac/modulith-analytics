package net.pvytykac.modularapp.license.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface LicenseRepository extends JpaRepository<LicenseEntity, String> {

    Page<LicenseEntity> findAllByApplicationId(String applicationId, Pageable pageable);

    Optional<LicenseEntity> findByApplicationIdAndId(String applicationId, String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LicenseEntity l WHERE l.applicationId = :applicationId and l.id = :id")
    Optional<LicenseEntity> findByApplicationIdAndIdForUpdate(@Param("applicationId") String applicationId, @Param("id") String id);

    @Modifying
    void deleteAllByApplicationId(String applicationId);

}
