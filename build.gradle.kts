plugins {
    java
    `java-library`
    `maven-publish`
    id("de.chojo.publishdata") version "1.4.0"
    alias(libs.plugins.spotless)
    alias(libs.plugins.indra.core)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.indra.sonatype)
}

publishData {
    useEldoNexusRepos(false)
    publishingVersion = "2.3.1"
}

group = "dev.chojo"
version = publishData.getVersion()
description =
    "Wrapper library around jackson to manage configuration files. Supporting different formats and allows high customization with reasonable defaults to start right away."

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.slf4j", "slf4j-api", "2.0.16")
    compileOnlyApi("org.jetbrains", "annotations", "24.1.0")
    api("com.fasterxml.jackson.core", "jackson-databind") {
        version {
            require("2.13.0")
            prefer("2.17.1")
        }
    }
    compileOnly("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
    compileOnly("com.fasterxml.jackson.dataformat", "jackson-dataformat-toml")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains", "annotations", "24.1.0")
    testImplementation("org.slf4j", "slf4j-api", "2.0.16")
    testImplementation("org.slf4j", "slf4j-simple", "2.0.16")
    testImplementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml")
    testImplementation("com.fasterxml.jackson.dataformat", "jackson-dataformat-toml")
    testImplementation("de.eldoria.jacksonbukkit", "paper", "1.2.0")
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

java{
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

indra {
    javaVersions {
        target(17)
        testWith(17)
    }

    github("rainbowdashlabs", "ocular") {
        ci(true)
    }

    lgpl3OrLaterLicense()

    signWithKeyFromPrefixedProperties("rainbowdashlabs")

    configurePublications {
        pom {
            developers {
                developer {
                    id.set("rainbowdashlabs")
                    name.set("Lilly Fülling")
                    email.set("mail@chojo.dev")
                    url.set("https://github.com/rainbowdashlabs")
                }
            }
        }
    }
}

indraSonatype {
    useAlternateSonatypeOSSHost("s01")
}

tasks {
    test {
        dependsOn(spotlessCheck)
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    compileJava {
        dependsOn(spotlessApply)
    }
}
