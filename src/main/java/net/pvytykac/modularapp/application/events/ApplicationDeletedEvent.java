package net.pvytykac.modularapp.application.events;

import java.time.Instant;

public record ApplicationDeletedEvent(String applicationId, String name, Instant deletedAt) {
}