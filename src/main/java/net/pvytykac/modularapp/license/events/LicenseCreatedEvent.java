package net.pvytykac.modularapp.license.events;

import java.math.BigDecimal;

public record LicenseCreatedEvent(String applicationId, String licenseId, String name, BigDecimal monthlyPrice) {
}