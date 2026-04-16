plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-kernel-errors"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
