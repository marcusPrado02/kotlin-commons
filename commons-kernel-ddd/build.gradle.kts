plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-kernel-errors"))
    api(project(":commons-kernel-result"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
