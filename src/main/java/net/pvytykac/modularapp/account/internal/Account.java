package net.pvytykac.modularapp.account.internal;

import java.time.Instant;
import java.util.Set;

record Account(String id, String externalId, Instant lastUsed, Set<String> licenseIds) {
}
