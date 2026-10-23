import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    `java-library`
    alias(libs.plugins.publishdata)
    alias(libs.plugins.spotless)
    alias(libs.plugins.vanniktech)
}

publishData {
    eldoNexus(dev = false)
    channels {
        clear()
        release("https://eldonexus.de/repository/maven-releases/") { publishToMavenCentral() }
        snapshot("https://eldonexus.de/repository/maven-snapshots/")
    }
}

description =
    "Wrapper library around jackson to manage configuration files. Supporting different formats and allows high customization with reasonable defaults to start right away."

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.slf4j.api)
    compileOnlyApi(libs.annotations)
    annotationProcessor(libs.annotations)
    api(libs.jackson.databind)
    compileOnly(libs.jackson.yaml)
    compileOnly(libs.jackson.toml)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
    testAnnotationProcessor(sourceSets.main.get().output)
    testImplementation(libs.annotations)
    testImplementation(libs.slf4j.api)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.jackson.yaml)
    testImplementation(libs.jackson.toml)
    testImplementation(libs.jacksonbukkit.paper)
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        palantirJavaFormat(libs.versions.palantir.get()).formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).links("https://docs.oracle.com/en/java/javase/21/docs/api/")
}

mavenPublishing{
    signAllPublications()
    publishToMavenCentral()

    pom{
        name.set("Ocular")
        description.set(project.description)
        inceptionYear.set("2025")
        url.set("https://github.com/rainbowdashlabs/ocular")
        licenses {
            license {
                name.set("LGPL-3.0-or-later")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
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
            developerConnection.set("scm:git:ssh://github.com/rainbowdashlabs/ocular.git")
        }
    }



    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = SourcesJar.Sources()
    ))
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register<Copy>("copyOverrideReference") {
        description = "Copies the generated override reference file to build/ocular/"
        from(sourceSets.main.get().output.classesDirs + sourceSets.test.get().output.classesDirs) {
            include("META-INF/ocular/overrides.md")
        }
        eachFile {
            relativePath = RelativePath(true, name)
        }
        includeEmptyDirs = false
        into(layout.buildDirectory.dir("ocular"))
    }

    named("classes") {
        finalizedBy("copyOverrideReference")
    }

    test {
        finalizedBy("copyOverrideReference")
    }
}
