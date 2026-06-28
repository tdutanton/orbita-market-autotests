plugins {
    java
    `java-library`
    id("io.qameta.allure") version "4.0.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val allureVersion = "2.34.0"
val restAssuredVersion = "6.0.0"
val junitVersion = "6.1.0"
val jacksonVersion = "2.21.3"
val assertjVersion = "3.27.7"
val aspectjVersion = "1.9.25"
val lombokVersion = "1.18.36"

dependencies {
// JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
// Rest Assured для API-тестов
    implementation("io.rest-assured:rest-assured:${restAssuredVersion}")
// Allure для отчетов
    testImplementation("io.qameta.allure:allure-junit5:${allureVersion}")
// Jackson для JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
// AssertJ для ассертов
    implementation("org.assertj:assertj-core:${assertjVersion}")

    // Для основного кода (src/main/java)
    compileOnly("org.projectlombok:lombok:${lombokVersion}")
    annotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    // Для тестов (src/test/java)
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")

    testImplementation("org.slf4j:slf4j-simple:2.0.13")
}

tasks.test {
    useJUnitPlatform()

    systemProperty("base.url", System.getProperty("base.url", "http://localhost:8080"))

    testLogging {
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

allure {
    version.set(allureVersion)
}

tasks.register("testAndReport") {
    dependsOn(tasks.test)
    finalizedBy(tasks.allureServe)
}