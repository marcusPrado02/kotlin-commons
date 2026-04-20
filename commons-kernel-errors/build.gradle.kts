plugins {
    id("kotlin-commons")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(libs.kotlinx.serialization.json)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
