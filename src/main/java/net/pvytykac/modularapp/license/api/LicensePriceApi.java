package net.pvytykac.modularapp.license.api;

import java.util.Collection;
import java.util.Map;

public interface LicensePriceApi {

    LicensePriceInfo getOneOrFallback(String applicationId, String licenseId);

    Map<String, LicensePriceInfo> getAllOrFallback(String applicationId, Collection<String> licenseIds);

}
