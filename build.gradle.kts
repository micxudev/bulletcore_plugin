plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.19.2")
}

tasks {
    compileJava {
        options.release = 21
    }
}
