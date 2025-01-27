# Ocular

Ocular is a library that allows managing multiple configuration files. It uses jackson to interact with different dataformats.
Everything is customizable, but reasonable defaults are given to start right away.

Latest version:  
![Maven Central Version](https://img.shields.io/maven-central/v/dev.chojo/ocular?style=for-the-badge)


=== "gradle.build.kts"

    ```java
    implementation("dev.chojo", "ocular", "version")
    ```

=== "pom.xml"

    ```xml
    <dependency>
        <groupId>dev.chojo</groupId>
        <artifactId>ocular</artifactId>
        <version>version</version>
    </dependency>
    ```

!!! warning

    You will need to import your desired data format for jackson additionally.
