package net.pvytykac.modularapp.user.events;

public record UserDeletedEvent(String userId, String displayName, String email) {
}