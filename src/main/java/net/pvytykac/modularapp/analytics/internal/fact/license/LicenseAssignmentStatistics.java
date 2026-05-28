package net.pvytykac.modularapp.analytics.internal.fact.license;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
public class LicenseAssignmentStatistics {

    String id;
    String name;
    BigDecimal monthlyCost;
    Long assignedCount;
    Long revokedCount;

}
