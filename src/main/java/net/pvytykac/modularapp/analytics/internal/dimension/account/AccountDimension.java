package net.pvytykac.modularapp.analytics.internal.dimension.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "dim_accounts",
        indexes = {
                @Index(name = "ix_dimAccounts_accountId", columnList = "accountId"),
                @Index(name = "ix_dimAccounts_applicationId", columnList = "applicationId"),
                @Index(name = "ix_dimAccounts_userId", columnList = "userId"),
                @Index(name = "ix_dimAccounts_lastUsed_asc", columnList = "lastUsed ASC"),
                @Index(name = "ix_dimAccounts_lastUsed_desc", columnList = "lastUsed DESC"),
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AccountDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Builder.Default
    boolean active = true;

    @NotNull
    String accountId;

    @NotNull
    String applicationId;

    @NotNull
    String userId;

    @NotNull
    Instant lastUsed;

}
