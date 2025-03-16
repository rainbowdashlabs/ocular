rootProject.name = "ocular"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            name = "EldoNexus"
            url = uri("https://eldonexus.de/repository/maven-public/")

        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.9.0")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            // plugins
            plugin("spotless", "com.diffplug.spotless").version("7.0.2")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")

            version("indra", "3.1.3")
            plugin("indra-core", "net.kyori.indra").versionRef("indra")
            plugin("indra-publishing", "net.kyori.indra.publishing").versionRef("indra")
            plugin("indra-sonatype", "net.kyori.indra.publishing.sonatype").versionRef("indra")

            version("slf4j", "2.0.17")
            library("slf4j-api", "org.slf4j", "slf4j-api").versionRef("slf4j")
        }

        create("testlibs") {
            version("slf4j", "2.0.17")
            library("slf4j-noop", "org.slf4j", "slf4j-nop").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
        }
    }
}
