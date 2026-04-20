allprojects {
    group = "com.marcusprado02.commons"
    version = "0.1.0-SNAPSHOT"
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
