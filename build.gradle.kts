plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.dokka")
}

apiValidation {
    ignoredProjects += setOf("commons-bom", "commons-testkit-testcontainers")
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = false
            }
        }
    }
}

allprojects {
    group = "io.github.marcusprado02.commons"
    version = property("version").toString()
}

subprojects {
    tasks.withType<org.jetbrains.dokka.gradle.AbstractDokkaLeafTask>().configureEach {
        dokkaSourceSets.configureEach {
            jdkVersion.set(21)
        }
    }
}

tasks.register("checkAll") {
    description = "Runs detekt, ktlintCheck, and test across all subprojects"
    group = "verification"
    dependsOn(subprojects.flatMap { sub ->
        listOf(
            sub.tasks.matching { it.name == "detekt" },
            sub.tasks.matching { it.name == "ktlintCheck" },
            sub.tasks.matching { it.name == "test" },
        )
    })
}
