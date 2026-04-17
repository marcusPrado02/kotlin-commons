plugins {
    id("kotlin-commons")
    id("org.jetbrains.kotlin.plugin.spring")
    id("io.spring.dependency-management")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.findVersion("spring-boot").get()}")
    }
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
}
