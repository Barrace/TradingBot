plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1. 0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "2.3.4"

    testImplementation(kotlin("test"))
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okio:okio:3.0.0")

    implementation("org.json:json:20210307")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:1.2.10")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("app.MainKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("app.jar")
}