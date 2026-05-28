@ApplicationModule(
        id = "analytics",
        allowedDependencies = {"application::events", "user::events", "account::events", "license::events", "license::api"}
)
package net.pvytykac.modularapp.analytics;

import org.springframework.modulith.ApplicationModule;