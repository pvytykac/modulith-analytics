package net.pvytykac.modularapp.analytics.internal.dimension.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
interface AccountDimensionRepository extends JpaRepository<AccountDimension, String> {

    Optional<AccountDimension> findByAccountId(String accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM AccountDimension d WHERE d.accountId = :accountId")
    Optional<AccountDimension> findByAccountIdForUpdate(@Param("accountId") String accountId);

    @Modifying
    @Query("UPDATE AccountDimension d SET d.active = false WHERE d.accountId = :accountId")
    void deactivateByAccountId(@Param("accountId") String accountId);

    @Modifying
    @Query("UPDATE AccountDimension d SET d.active = false WHERE d.applicationId = :applicationId")
    void deactivateByApplicationId(@Param("applicationId") String applicationId);

    @Modifying
    @Query("UPDATE AccountDimension d SET d.active = false WHERE d.userId = :userId")
    void deactivateByUserId(@Param("userId") String userId);

}
