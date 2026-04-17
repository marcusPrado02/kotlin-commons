rootProject.name = "kotlin-commons"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    "commons-kernel-core",
    "commons-kernel-errors",
    "commons-kernel-result",
    "commons-kernel-ddd",
    "commons-kernel-time",
    "commons-ports-persistence",
    "commons-ports-messaging",
    "commons-ports-http",
    "commons-ports-cache",
    "commons-ports-email",
    "commons-bom",
    "commons-testkit-testcontainers",
    "commons-adapters-persistence-jpa",
    "commons-adapters-cache-redis",
    "commons-adapters-messaging-kafka",
    "commons-adapters-http-okhttp",
    "commons-adapters-email-smtp",
)
