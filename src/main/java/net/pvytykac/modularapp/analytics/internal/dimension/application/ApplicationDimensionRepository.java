package net.pvytykac.modularapp.analytics.internal.dimension.application;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface ApplicationDimensionRepository extends JpaRepository<ApplicationDimension, String> {

    Optional<ApplicationDimension> findByApplicationId(String applicationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM ApplicationDimension d WHERE d.applicationId = :applicationId")
    Optional<ApplicationDimension> findByIdForUpdate(@Param("applicationId") String applicationId);

    @Modifying
    @Query("UPDATE ApplicationDimension d SET d.active = false WHERE d.applicationId = :applicationId")
    void deactivateByApplicationId(@Param("applicationId") String applicationId);

}
