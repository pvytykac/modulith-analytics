package net.pvytykac.modularapp.account.internal;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(
        name = "accounts",
        uniqueConstraints = @UniqueConstraint(name = "uq_accounts_applicationId_externalId", columnNames = {"applicationId", "externalId"}),
        indexes = {
                @Index(name = "ix_accounts_applicationId", columnList = "applicationId"),
                @Index(name = "ix_accounts_userId", columnList = "userId"),
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @NotNull
    @PastOrPresent
    Instant lastUsed;

    @NotBlank
    String externalId;

    @CollectionTable(name = "account_licenses")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    Set<String> licenseIds;

    @NotNull
    String applicationId;

    @NotNull
    String userId;

}
