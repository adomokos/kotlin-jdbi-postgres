import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    base
    kotlin("jvm") version "1.6.20" apply false

    // Printing unit tests in mocha-style
    id("com.adarshr.test-logger") version "3.2.0"
    // Cyclomatic complexity calculator
    id("io.gitlab.arturbosch.detekt").version("1.19.0")
    // Checking for newer versions
    id("com.github.ben-manes.versions").version("0.42.0")
    // ktlint plugin
    id("org.jlleitschuh.gradle.ktlint").version("10.2.1")
}

// Declaring variables to be populated from gradle.properties
val arrowVersion: String by project
val kotestVersion: String by project
val kotestAssertionsVersion: String by project
val kotestExtensionsVersion: String by project
val kotlinLoggingVersion: String by project
val ktlintVersion: String by project
val logbackVersion: String by project
val jacocoVersion: String by project
val testcontainersVersion: String by project

allprojects {
    group = "jdbiktpg"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")

    ktlint {
        version.set(ktlintVersion)
        enableExperimentalRules.set(true)
        filter {
            // exclude any built files
            exclude({ it.file.absolutePath.contains("/generated/") })
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")

        // Arrow
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-stm:$arrowVersion")

        // logging
        implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")

        // kotest
        testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestAssertionsVersion")
        testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-arrow:$kotestAssertionsVersion")
        testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:$kotestExtensionsVersion")

        // testcontainers
        testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
        testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()

            testlogger {
                setTheme("mocha")
            }
        }

        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "14"
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    detekt {
        buildUponDefaultConfig = true
        config = files("../resources/detekt-config.yml")
    }
}
