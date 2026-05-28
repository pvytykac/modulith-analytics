package net.pvytykac.modularapp.analytics.internal.dimension.application;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "dim_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApplicationDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Builder.Default
    boolean active = true;

    @NotBlank
    String applicationId;

    @NotBlank
    String name;

    @NotNull
    Instant createdAt;
}
