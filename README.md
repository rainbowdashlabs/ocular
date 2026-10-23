# Ocular

[![Maven Central](https://img.shields.io/maven-central/v/dev.chojo/ocular?style=for-the-badge&label=Release)](https://central.sonatype.com/artifact/dev.chojo/ocular)
[![Snapshot](https://img.shields.io/nexus/s/dev.chojo/ocular?server=https%3A%2F%2Feldonexus.de&style=for-the-badge&label=Snapshot&color=orange)](https://eldonexus.de/#browse/browse:maven-snapshots:dev%2Fchojo%2Focular)

Manage configuration files with Jackson. Ocular reads, writes and reloads typed configuration classes in JSON,
YAML or TOML, with reasonable defaults to start right away and room for customization when you need it.

**Documentation: [docs.chojo.dev/ocular](https://docs.chojo.dev/ocular)**

## Requirements

- Java 21 or newer
- For YAML or TOML files, the matching Jackson 3 module: `tools.jackson.dataformat:jackson-dataformat-yaml` or
  `jackson-dataformat-toml` (Ocular brings `jackson-databind` itself)

## Dependency

Gradle (Kotlin DSL):

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.chojo:ocular:2.3.2")
}
```

Maven:

```xml
<dependency>
    <groupId>dev.chojo</groupId>
    <artifactId>ocular</artifactId>
    <version>2.3.2</version>
</dependency>
```

Snapshots are on EldoNexus: `https://eldonexus.de/repository/maven-snapshots/`.

## License

LGPL-3.0-or-later, see [LICENSE.md](LICENSE.md).
