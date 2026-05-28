package net.pvytykac.modularapp.analytics.internal.dimension.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface UserDimensionRepository extends JpaRepository<UserDimension, String> {

    Optional<UserDimension> findByUserId(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM UserDimension d WHERE d.userId = :userId")
    Optional<UserDimension> findByUserIdForUpdate(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserDimension d SET d.active = false WHERE d.userId = :userId")
    void deactivateByUserId(@Param("userId") String userId);

}
