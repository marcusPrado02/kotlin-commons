plugins {
    id("kotlin-commons")
    id("org.jetbrains.kotlin.plugin.spring")
}

val libs = the<VersionCatalogsExtension>().named("libs")
val springBootVersion = libs.findVersion("spring-boot").get().toString()

// Use Gradle native platform() instead of Spring DM plugin.
// platform() only affects configurations that explicitly include it,
// so detekt's configuration is not contaminated by Spring Boot's Kotlin version pin.
dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
}
