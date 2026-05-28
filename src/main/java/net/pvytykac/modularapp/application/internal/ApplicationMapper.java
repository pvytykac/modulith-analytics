package net.pvytykac.modularapp.application.internal;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring"
)
interface ApplicationMapper {

    Application entityToRepresentation(ApplicationEntity entity);

}
