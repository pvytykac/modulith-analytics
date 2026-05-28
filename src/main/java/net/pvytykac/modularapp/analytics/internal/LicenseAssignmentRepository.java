package net.pvytykac.modularapp.analytics.internal;

import net.pvytykac.modularapp.analytics.internal.fact.license.LicenseAssignmentFact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface LicenseAssignmentRepository extends JpaRepository<LicenseAssignmentFact, String> {

    @Query("""
                SELECT
                    new net.pvytykac.modularapp.analytics.internal.LicenseAssignmentBreakdown(
                        date(la.assignmentTime) as date,
                        SUM(case when (la.eventType = ASSIGNED) THEN 1 ELSE 0 END) as assingedCount,
                        SUM(case when (la.eventType = ASSIGNED) THEN la.licenseMonthlyPrice ELSE 0 END) as assingedCost,
                        SUM(case when (la.eventType = REVOKED) THEN 1 ELSE 0 END) as revokedCount,
                        SUM(case when (la.eventType = REVOKED) THEN la.licenseMonthlyPrice ELSE 0 END) as revokedCost
                    )
                FROM LicenseAssignmentFact la
                JOIN AccountDimension dimAcc ON (dimAcc.id = la.accountDimensionKey)
                JOIN LicenseDimension dimLic ON (dimLic.id = la.licenseDimensionKey)
                JOIN UserDimension dimUser ON (dimUser.id = la.userDimensionKey)
                JOIN ApplicationDimension dimApp ON (dimApp.id = la.applicationDimensionKey)
                WHERE
                    la.assignmentTime >= :startTime
                    AND la.assignmentTime < :endTime
                    AND (:applicationId IS NULL OR dimApp.applicationId = :applicationId)
                    AND (:userId IS NULL OR dimUser.userId = :userId)
                    AND (:licenseId IS NULL OR dimLic.licenseId = :licenseId)
                    AND (:accountId IS NULL OR dimAcc.accountId = :accountId)
                    AND (:email IS NULL OR dimUser.email = :email)
                    AND (:minLicenseCost IS NULL OR dimLic.cost >= :minLicenseCost)
                    AND (:maxLicenseCost IS NULL OR dimLic.cost < :maxLicenseCost)
                    AND (coalesce(:minApplicationCreatedAt, null) IS NULL OR dimApp.createdAt >= :minApplicationCreatedAt)
                    AND (coalesce(:maxApplicationCreatedAt, null) IS NULL OR dimApp.createdAt < :maxApplicationCreatedAt)
                    AND (coalesce(:minLastUsed, null) IS NULL OR dimAcc.lastUsed >= :minLastUsed)
                    AND (coalesce(:maxLastUsed, null) IS NULL OR dimAcc.lastUsed < :maxLastUsed)
                GROUP BY 1
                ORDER BY 1 ASC
            """)
    List<LicenseAssignmentBreakdown> getDailyLicenseAssignmentBreakdown(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("applicationId") String applicationId,
            @Param("accountId") String accountId,
            @Param("userId") String userId,
            @Param("licenseId") String licenseId,
            @Param("email") String email,
            @Param("minLicenseCost") BigDecimal minLicenseCost,
            @Param("maxLicenseCost") BigDecimal maxLicenseCost,
            @Param("minApplicationCreatedAt") Instant minApplicationCreatedAt,
            @Param("maxApplicationCreatedAt") Instant maxApplicationCreatedAt,
            @Param("minLastUsed") Instant minLastUsed,
            @Param("maxLastUsed") Instant maxLastUsed);

    @Query(value = """
                SELECT
                    new net.pvytykac.modularapp.analytics.internal.LicenseAssignmentStatistics(
                        dimLic.licenseId,
                        dimLic.name,
                        dimLic.cost,
                        SUM(case when (la.eventType = ASSIGNED) THEN 1 ELSE 0 END) as assignedCount,
                        SUM(case when (la.eventType = REVOKED) THEN 1 ELSE 0 END) as revokedCount
                    )
                FROM LicenseAssignmentFact la
                JOIN LicenseDimension dimLic ON (dimLic.id = la.licenseDimensionKey)
                JOIN AccountDimension dimAcc ON (dimAcc.id = la.accountDimensionKey)
                JOIN UserDimension dimUser ON (dimUser.id = la.userDimensionKey)
                JOIN ApplicationDimension dimApp ON (dimApp.id = la.applicationDimensionKey)
                WHERE
                    la.assignmentTime >= :startTime
                    AND la.assignmentTime < :endTime
                    AND (:applicationId IS NULL OR dimApp.applicationId = :applicationId)
                    AND (:userId IS NULL OR dimUser.userId = :userId)
                    AND (:licenseId IS NULL OR dimLic.licenseId = :licenseId)
                    AND (:accountId IS NULL OR dimAcc.accountId = :accountId)
                    AND (:email IS NULL OR dimUser.email = :email)
                    AND (:minLicenseCost IS NULL OR dimLic.cost >= :minLicenseCost)
                    AND (:maxLicenseCost IS NULL OR dimLic.cost < :maxLicenseCost)
                    AND (coalesce(:minApplicationCreatedAt, null) IS NULL OR dimApp.createdAt >= :minApplicationCreatedAt)
                    AND (coalesce(:maxApplicationCreatedAt, null) IS NULL OR dimApp.createdAt < :maxApplicationCreatedAt)
                    AND (coalesce(:minLastUsed, null) IS NULL OR dimAcc.lastUsed >= :minLastUsed)
                    AND (coalesce(:maxLastUsed, null) IS NULL OR dimAcc.lastUsed < :maxLastUsed)
                GROUP BY 1,2,3
            """,
            countQuery = """
                SELECT
                    COUNT(DISTINCT la.licenseDimensionKey)
                FROM LicenseAssignmentFact la
                JOIN LicenseDimension dimLic ON (dimLic.id = la.licenseDimensionKey)
                JOIN AccountDimension dimAcc ON (dimAcc.id = la.accountDimensionKey)
                JOIN UserDimension dimUser ON (dimUser.id = la.userDimensionKey)
                JOIN ApplicationDimension dimApp ON (dimApp.id = la.applicationDimensionKey)
                WHERE
                    la.assignmentTime >= :startTime
                    AND la.assignmentTime < :endTime
                    AND (:applicationId IS NULL OR dimApp.applicationId = :applicationId)
                    AND (:userId IS NULL OR dimUser.userId = :userId)
                    AND (:licenseId IS NULL OR dimLic.licenseId = :licenseId)
                    AND (:accountId IS NULL OR dimAcc.accountId = :accountId)
                    AND (:email IS NULL OR dimUser.email = :email)
                    AND (:minLicenseCost IS NULL OR dimLic.cost >= :minLicenseCost)
                    AND (:maxLicenseCost IS NULL OR dimLic.cost < :maxLicenseCost)
                    AND (coalesce(:minApplicationCreatedAt, null) IS NULL OR dimApp.createdAt >= :minApplicationCreatedAt)
                    AND (coalesce(:maxApplicationCreatedAt, null) IS NULL OR dimApp.createdAt < :maxApplicationCreatedAt)
                    AND (coalesce(:minLastUsed, null) IS NULL OR dimAcc.lastUsed >= :minLastUsed)
                    AND (coalesce(:maxLastUsed, null) IS NULL OR dimAcc.lastUsed < :maxLastUsed)
            """)
    Page<LicenseAssignmentStatistics> getLicenseAssignmentStatistics(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("applicationId") String applicationId,
            @Param("accountId") String accountId,
            @Param("userId") String userId,
            @Param("licenseId") String licenseId,
            @Param("email") String email,
            @Param("minLicenseCost") BigDecimal minLicenseCost,
            @Param("maxLicenseCost") BigDecimal maxLicenseCost,
            @Param("minApplicationCreatedAt") Instant minApplicationCreatedAt,
            @Param("maxApplicationCreatedAt") Instant maxApplicationCreatedAt,
            @Param("minLastUsed") Instant minLastUsed,
            @Param("maxLastUsed") Instant maxLastUsed,
            Pageable pageable);

}
