package net.pvytykac.modularapp.analytics.internal.fact.license;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface LicenseAssignmentFactRepository extends JpaRepository<LicenseAssignmentFact, Integer> {
}
