package net.pvytykac.modularapp.analytics.internal.dimension.license;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface LicenseDimensionRepository extends JpaRepository<LicenseDimension, String> {

    Optional<LicenseDimension> findByLicenseId(String licenseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM LicenseDimension d WHERE d.licenseId = :licenseId")
    Optional<LicenseDimension> findByLicenseIdForUpdate(@Param("licenseId") String licenseId);

    @Modifying
    @Query("UPDATE LicenseDimension d SET d.active = false WHERE d.licenseId = :licenseId")
    void deactivateByLicenseId(@Param("licenseId") String licenseId);

    @Modifying
    @Query("UPDATE LicenseDimension d SET d.active = false WHERE d.applicationId = :applicationId")
    void deactivateByApplicationId(@Param("applicationId") String applicationId);

}
