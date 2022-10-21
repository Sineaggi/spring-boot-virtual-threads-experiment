pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/milestone/")
    }
    plugins {
        id("org.springframework.boot") version "3.0.0-RC1"
        id("io.spring.dependency-management") version "1.0.14.RELEASE"
    }
}
