package net.pvytykac.modularapp.analytics.internal.dimension;

import lombok.RequiredArgsConstructor;
import net.pvytykac.modularapp.analytics.internal.dimension.account.AccountDimensionService;
import net.pvytykac.modularapp.analytics.internal.dimension.application.ApplicationDimensionService;
import net.pvytykac.modularapp.analytics.internal.dimension.license.LicenseDimensionService;
import net.pvytykac.modularapp.analytics.internal.dimension.user.UserDimensionService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DimensionFacade {

    private final ApplicationDimensionService applicationDimensionService;
    private final AccountDimensionService accountDimensionService;
    private final LicenseDimensionService licenseDimensionService;
    private final UserDimensionService userDimensionService;

    public String getApplicationDimensionKey(String applicationId) {
        return applicationDimensionService.getDimensionKey(applicationId);
    }

    public String getAccountDimensionKey(String accountId) {
        return accountDimensionService.getDimensionKey(accountId);
    }

    public String getLicenseDimensionKey(String licenseId) {
        return licenseDimensionService.getDimensionKey(licenseId);
    }

    public String getUserDimensionKey(String userId) {
        return userDimensionService.getDimensionKey(userId);
    }
}
