package com.meteo.app.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class HexagonalArchitectureTest {

    @Test
    void elDominioNoDependeDeInfraestructuraNiApplication() {
        var imported = new ClassFileImporter().importPackages("com.meteo.app");
        ArchRule rule = noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..application..",
                        "org.springframework..", "org.springdoc..");
        rule.check(imported);
    }

    @Test
    void losPuertosInSoloSonInterfaces() {
        var imported = new ClassFileImporter().importPackages("com.meteo.app.domain.port.in");
        classes().that().resideInAPackage("..port.in..").should().beInterfaces().check(imported);
    }

    @Test
    void sinCiclosEntreCapas() {
        var imported = new ClassFileImporter().importPackages("com.meteo.app");
        SlicesRuleDefinition.slices().matching("com.meteo.app.(*)..").should().beFreeOfCycles().check(imported);
    }
}
