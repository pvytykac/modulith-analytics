package net.pvytykac.modularapp.analytics.internal;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LicenseAssignmentStatistics {

    private final String id;
    private final String name;
    private final BigDecimal monthlyCost;
    private final Long assignedCount;
    private final Long revokedCount;

}
