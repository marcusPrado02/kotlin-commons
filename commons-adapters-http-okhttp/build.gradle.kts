plugins {
    id("kotlin-commons")
}

dependencies {
    api(project(":commons-ports-http"))
    api(libs.okhttp)
    api(libs.resilience4j.circuitbreaker)
    api(libs.kotlinx.coroutines.core)
    implementation("org.slf4j:slf4j-api:2.0.17")
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
