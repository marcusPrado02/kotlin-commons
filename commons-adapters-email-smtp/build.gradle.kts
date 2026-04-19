plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-email"))
    api(libs.angus.mail)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.greenmail)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
