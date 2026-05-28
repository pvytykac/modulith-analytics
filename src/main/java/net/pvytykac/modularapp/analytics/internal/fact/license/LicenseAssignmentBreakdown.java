package net.pvytykac.modularapp.analytics.internal.fact.license;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@AllArgsConstructor
public class LicenseAssignmentBreakdown {

    Object date;
    Long assignedCount;
    BigDecimal assignedCost;
    Long revokedCount;
    BigDecimal revokedCost;

}