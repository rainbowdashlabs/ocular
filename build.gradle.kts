plugins {
    id("java")
    `java-library`
    `maven-publish`
}

group = "dev.chojo"
version = "1.0.0"

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
}

tasks {
    test {
        useJUnitPlatform()
    }
}
