package net.pvytykac.modularapp.license.internal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "licenses",
        uniqueConstraints = @UniqueConstraint(name = "uq_licenses_applicationId_name", columnNames = {"applicationId", "name"}),
        indexes = {
                @Index(name = "ix_licenses_applicationId", columnList = "applicationId"),
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LicenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @NotBlank
    String name;

    @NotNull
    @PositiveOrZero
    BigDecimal monthlyPrice;

    @NotNull
    String applicationId;

}
