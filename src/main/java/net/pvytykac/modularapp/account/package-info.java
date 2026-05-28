@ApplicationModule(
        id = "account",
        allowedDependencies = {"application::events", "application::api", "user::events", "license::api"}
)
package net.pvytykac.modularapp.account;

import org.springframework.modulith.ApplicationModule;