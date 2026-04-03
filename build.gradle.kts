import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    `java-library`
    id("de.chojo.publishdata") version "1.4.0"
    alias(libs.plugins.spotless)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

// OpenRewrite configuration will be passed via command line properties

publishData {
    useEldoNexusRepos(false)
    publishingVersion = "2.1.0"
}

group = "dev.chojo"
version = publishData.getVersion()
description =
    "Wrapper library around jackson to manage configuration files. Supporting different formats and allows high customization with reasonable defaults to start right away."

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.slf4j", "slf4j-api", "2.0.17")
    compileOnlyApi("org.jetbrains", "annotations", "26.1.0")
    annotationProcessor("org.jetbrains:annotations:26.1.0") // to avoid warnings if any
    // ... rest of dependencies
    api("tools.jackson.core", "jackson-databind") {
        version {
            require("3.0.0")
            prefer("3.1.1")
        }
    }
    compileOnly("tools.jackson.dataformat", "jackson-dataformat-yaml")
    compileOnly("tools.jackson.dataformat", "jackson-dataformat-toml")

    testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-params:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
    testAnnotationProcessor(sourceSets.main.get().output)
    testImplementation("org.jetbrains", "annotations", "26.1.0")
    testImplementation("org.slf4j", "slf4j-api", "2.0.17")
    testImplementation("org.slf4j", "slf4j-simple", "2.0.17")
    testImplementation("tools.jackson.dataformat", "jackson-dataformat-yaml")
    testImplementation("tools.jackson.dataformat", "jackson-dataformat-toml")
    testImplementation("de.eldoria.jacksonbukkit", "paper", "2.0.0")
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

mavenPublishing{
    signAllPublications()
    publishToMavenCentral()

    coordinates(groupId = "dev.chojo", artifactId = "ocular", version = publishData.getVersion())

    pom{
        name.set("Ocular")
        description.set(project.description)
        inceptionYear.set("2025")
        url.set("https://github.com/rainbowdashlabs/ocular")
        licenses {
            license {
                name.set("LGPL-3.0")
                url.set("https://opensource.org/license/lgpl-3-0")
            }
        }

        developers{
            developer{
                id.set("rainbowdashlabs")
                name.set("Nora Fülling")
                email.set("mail@chojo.dev")
                url.set("https://github.com/rainbowdashlabs")
            }
        }

        scm{
            url.set("https://github.com/rainbowdashlabs/ocular")
            connection.set("scm:git:git://github.com/rainbowdashlabs/ocular.git")
            developerConnection.set("scm:git:ssh://github.com/racinbowdashlabs/ocular.git")
        }
    }



    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
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
