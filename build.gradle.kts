import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
    }
}

plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
}

group = "org.catafratta.strukt"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    val autoServiceVersion = "1.0-rc7"
    val kotlinPoetVersion = "1.7.2"

    implementation(kotlin("stdlib"))

    implementation("com.google.auto.service:auto-service:$autoServiceVersion")
    kapt("com.google.auto.service:auto-service:$autoServiceVersion")

    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-metadata:$kotlinPoetVersion")

    testImplementation("junit:junit:4.13")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.6")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<DokkaTask>("dokkaHtml") {
    outputDirectory.set(rootProject.rootDir.resolve("docs"))

    dokkaSourceSets {
        named("main") {
            moduleName.set("Strukt ${project.version}")
        }
    }
}
