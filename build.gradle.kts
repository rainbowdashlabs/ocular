import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    java
    `java-library`
    id("de.chojo.publishdata") version "1.4.0"
    id("org.openrewrite.rewrite") version "latest.release"
    alias(libs.plugins.spotless)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

// OpenRewrite configuration will be passed via command line properties

publishData {
    useEldoNexusRepos(false)
    publishingVersion = "2.0.0"
}

group = "dev.chojo"
version = publishData.getVersion()
description =
    "Wrapper library around jackson to manage configuration files. Supporting different formats and allows high customization with reasonable defaults to start right away."

rewrite {
    activeRecipe("org.openrewrite.java.jackson.UpgradeJackson_2_3")
    setExportDatatables(true)
}
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.slf4j", "slf4j-api", "2.0.17")
    compileOnlyApi("org.jetbrains", "annotations", "26.0.2")
    api("tools.jackson.core", "jackson-databind") {
        version {
            require("3.0.0")
            prefer("3.1.1")
        }
    }
    compileOnly("tools.jackson.dataformat", "jackson-dataformat-yaml")
    compileOnly("tools.jackson.dataformat", "jackson-dataformat-toml")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.jetbrains", "annotations", "26.0.2")
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
