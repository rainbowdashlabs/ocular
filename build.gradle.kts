import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    `java-library`
    id("de.chojo.publishdata") version "1.4.0"
    alias(libs.plugins.spotless)
    id("com.vanniktech.maven.publish") version "0.30.0"
}

publishData {
    useEldoNexusRepos(false)
    publishingVersion = "1.0.1"
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

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

mavenPublishing{
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

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
                name.set("Lilly Fülling")
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
        sourcesJar = true
    ))
}

//
//indra {
//
//    publishReleasesTo("central", "https://central.sonatype.com")
//
//    javaVersions {
//        target(17)
//        testWith(17)
//    }
//
//    github("rainbowdashlabs", "ocular") {
//        ci(true)
//    }
//
//    lgpl3OrLaterLicense()
//
//    signWithKeyFromPrefixedProperties("rainbowdashlabs")
//
//    configurePublications {
//        pom {
//            developers {
//                developer {
//                    id.set("rainbowdashlabs")
//                    name.set("Lilly Fülling")
//                    email.set("mail@chojo.dev")
//                    url.set("https://github.com/rainbowdashlabs")
//                }
//            }
//        }
//    }
//}

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
