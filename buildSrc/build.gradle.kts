plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// Plugin versions below MUST be kept in sync with gradle/libs.versions.toml
// buildSrc cannot consume the root project's version catalog (Gradle limitation)
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.1.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.8")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:14.2.0")
    // Spring convention plugin dependencies
    implementation("org.jetbrains.kotlin:kotlin-allopen:2.1.0")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("org.jetbrains.kotlinx:binary-compatibility-validator:0.18.1")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.0.0")
    // Maven Central publishing
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.29.0")
}
