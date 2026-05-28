package net.pvytykac.modularapp.analytics.internal;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LicenseAssignmentBreakdown {

    private final Object date;
    private final Long assignedCount;
    private final BigDecimal assignedCost;
    private final Long revokedCount;
    private final BigDecimal revokedCost;

}