package net.pvytykac.modularapp.license.events;

import java.math.BigDecimal;

public record LicenseUpdatedEvent(String applicationId, String licenseId, String name, BigDecimal monthlyPrice) {
}