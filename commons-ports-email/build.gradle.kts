plugins {
    id("kotlin-commons")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
