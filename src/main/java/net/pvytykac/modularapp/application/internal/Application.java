package net.pvytykac.modularapp.application.internal;

import java.time.Instant;

record Application(String id, String name, Instant createdAt) {
}
