package net.pvytykac.modularapp.license.events;

import java.math.BigDecimal;

public record LicenseDeletedEvent(String applicationId, String licenseId, String name, BigDecimal monthlyPrice) {
}