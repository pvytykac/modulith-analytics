package net.pvytykac.modularapp.user.internal;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring"
)
interface UserMapper {

    User entityToRepresentation(UserEntity entity);

}
