package com.example.onboarding;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  private static final String APPLICATION_PACKAGE = "com.example.onboarding..";
  private static final String DOMAIN_PACKAGE = "com.example.onboarding.domain..";
  private static final String UTIL_PACKAGE = "com.example.onboarding.util..";

  @Test
  void domainShouldOnlyDependOnDomainOrUtilPackages() {
    var applicationClasses = new ClassFileImporter().importPackages("com.example.onboarding");
    var allowedDependencies =
        resideInAnyPackage(DOMAIN_PACKAGE, UTIL_PACKAGE)
            .or(resideInAPackage(APPLICATION_PACKAGE).negate());

    classes()
        .that()
        .resideInAPackage(DOMAIN_PACKAGE)
        .should()
        .onlyDependOnClassesThat(allowedDependencies)
        .check(applicationClasses);
  }
}
