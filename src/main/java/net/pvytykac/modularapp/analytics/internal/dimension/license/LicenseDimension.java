package net.pvytykac.modularapp.analytics.internal.dimension.license;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dim_licenses",
        indexes = {
                @Index(name = "ix_dimLicense_name", columnList = "name"),
                @Index(name = "ix_dimLicense_cost_asc", columnList = "cost ASC"),
                @Index(name = "ix_dimLicense_cost_desc", columnList = "cost DESC"),
                @Index(name = "ix_dimLicense_licenseId", columnList = "licenseId"),
                @Index(name = "ix_dimLicense_applicationId", columnList = "applicationId"),
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class LicenseDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Builder.Default
    boolean active = true;

    @NotBlank
    String licenseId;

    @NotBlank
    String applicationId;

    @NotBlank
    String name;

    @NotNull
    BigDecimal cost;
}
