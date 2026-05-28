package net.pvytykac.modularapp.analytics.internal.dimension.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dim_users",
        indexes = {
                @Index(name = "ix_dimUsers_displayName", columnList = "displayName"),
                @Index(name = "ix_dimUsers_email", columnList = "email"),
                @Index(name = "ix_dimUsers_userId", columnList = "userId"),
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Builder.Default
    boolean active = true;

    @NotBlank
    String userId;

    @NotBlank
    String displayName;

    @NotBlank
    String email;

}
