package net.pvytykac.modularapp;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModulithStructureTest {

    @Test
    void verifiesModulithStructure() {
        ApplicationModules.of(App.class)
                .verify();
    }

    @Test
    void writeDocumentationUml() {
        new Documenter(ApplicationModules.of(App.class))
                .writeDocumentation()
                .writeAggregatingDocument();
    }
}
