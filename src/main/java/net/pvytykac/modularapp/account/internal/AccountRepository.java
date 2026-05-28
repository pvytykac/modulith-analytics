package net.pvytykac.modularapp.account.internal;

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
interface AccountRepository extends JpaRepository<AccountEntity, String> {

    Page<AccountEntity> findAllByApplicationId(String applicationId, Pageable pageable);

    Optional<AccountEntity> findByApplicationIdAndId(String applicationId, String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.applicationId = :applicationId AND a.id = :id")
    Optional<AccountEntity> findByApplicationIdAndIdForUpdate(@Param("applicationId") String applicationId,
                                                              @Param("id") String id);

    @Modifying
    void deleteAllByApplicationId(String applicationId);

    @Modifying
    void deleteAllByUserId(String applicationId);
}
