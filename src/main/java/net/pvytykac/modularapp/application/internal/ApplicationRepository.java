package net.pvytykac.modularapp.application.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface ApplicationRepository extends JpaRepository<ApplicationEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApplicationEntity a WHERE a.id = :id")
    Optional<ApplicationEntity> findByIdForUpdate(@Param("id") String id);

}
