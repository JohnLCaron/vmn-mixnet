buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version "1.9.10"
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)

    application
    id ("maven-publish")
}

repositories {
    mavenCentral()
}

group = "vmn-mixnet"
version = "2.1-SNAPSHOT"

dependencies {
    implementation(files("libs/egk-ec-2.1-SNAPSHOT.jar"))
    implementation(files("libs/verificatum-vcr-3.1.0.jar"))
    implementation(files("libs/verificatum-vmn-3.1.0.jar"))
    implementation(files("libs/verificatum-vecj-2.2.0.jar"))
    implementation(files("libs/verificatum-vmgj-1.3.0.jar"))

    implementation(libs.bundles.eglib)
    implementation(libs.bundles.xmlutil)

    implementation(libs.oshai.logging)
    implementation(libs.logback.classic)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("MainKt")
}

// publish github package
// https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/JohnLCaron/egk-rave")
            credentials {
                username = project.findProperty("github.user") as String? ?: System.getenv("GITHUB_USER")
                password = project.findProperty("github.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "org.cryptobiotic"
            artifactId = "egk-rave"
            version = "0.3.1"
        }
    }
}
