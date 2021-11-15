package eu.pledgerproject.confservice;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {

        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("eu.pledgerproject.confservice");

        noClasses()
            .that()
                .resideInAnyPackage("eu.pledgerproject.confservice.service..")
            .or()
                .resideInAnyPackage("eu.pledgerproject.confservice.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..eu.pledgerproject.confservice.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses);
    }
}
