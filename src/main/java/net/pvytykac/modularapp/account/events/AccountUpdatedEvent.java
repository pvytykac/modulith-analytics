package net.pvytykac.modularapp.account.events;

import net.pvytykac.modularapp.license.api.LicensePriceInfo;

import java.time.Instant;
import java.util.Collection;

public record AccountUpdatedEvent(String applicationId, String userId, String accountId, String externalId,
                                  Instant lastUsed, Collection<LicensePriceInfo> assignedLicenses,
                                  Collection<LicensePriceInfo> revokedLicenses, Instant timestamp) {
}