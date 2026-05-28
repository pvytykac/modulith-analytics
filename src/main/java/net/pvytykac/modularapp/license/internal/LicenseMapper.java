package net.pvytykac.modularapp.license.internal;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring"
)
interface LicenseMapper {

    License entityToRepresentation(LicenseEntity entity);

}
