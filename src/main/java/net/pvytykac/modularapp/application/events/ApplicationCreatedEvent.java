package net.pvytykac.modularapp.application.events;

import java.time.Instant;

public record ApplicationCreatedEvent(String applicationId, String name, Instant createdAt) {
}