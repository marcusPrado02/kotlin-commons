plugins {
    id("kotlin-commons-spring")
}

dependencies {
    api(project(":commons-ports-cache"))
    api(project(":commons-ports-persistence"))
    api("org.springframework.boot:spring-boot-starter-data-redis")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api(libs.kotlinx.coroutines.core)
    implementation(libs.slf4j.api)
    testImplementation(project(":commons-testkit-testcontainers"))
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
