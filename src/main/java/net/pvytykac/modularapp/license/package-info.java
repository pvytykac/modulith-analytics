@ApplicationModule(
        id = "license",
        allowedDependencies = {"application::events", "application::api"}
)
package net.pvytykac.modularapp.license;

import org.springframework.modulith.ApplicationModule;