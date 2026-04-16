plugins {
    `java-platform`
}

group = "com.marcusprado02.commons"
version = "0.1.0-SNAPSHOT"

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":commons-kernel-core"))
        api(project(":commons-kernel-errors"))
        api(project(":commons-kernel-result"))
        api(project(":commons-kernel-ddd"))
        api(project(":commons-kernel-time"))
        api(project(":commons-ports-persistence"))
        api(project(":commons-ports-messaging"))
        api(project(":commons-ports-http"))
        api(project(":commons-ports-cache"))
        api(project(":commons-ports-email"))
    }
}
