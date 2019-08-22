plugins {
    base
    idea

    kotlin("jvm") version Versions.kotlin apply false
}

// set gav for project and repos
allprojects {
    group = "io.holunda.talk.camundacon"
    version = "0.0.1-SNAPSHOT"


    apply {
        from("${rootProject.projectDir}/gradle/repositories.gradle.kts")
    }
}

dependencies {
    // Make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        archives(it)
    }
}
