package net.pvytykac.modularapp.account.internal;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = "spring"
)
interface AccountMapper {

    Account entityToRepresentation(AccountEntity entity);

}
