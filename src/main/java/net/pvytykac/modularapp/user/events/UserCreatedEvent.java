package net.pvytykac.modularapp.user.events;

public record UserCreatedEvent(String userId, String displayName, String email) {
}