package net.pvytykac.modularapp.application.api;

import java.util.function.Supplier;

public interface ApplicationApi {

    <T> T mapIfApplicationExists(String applicationId, Supplier<T> supplier);

}
