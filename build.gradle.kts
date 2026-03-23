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
    maven {
        url = uri("https://repo.extendedclip.com/releases/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("tools.jackson.core:jackson-databind:3.0.0")
    compileOnly("me.clip:placeholderapi:2.12.2")
}

tasks {
    compileJava {
        options.release = 21
    }
}
