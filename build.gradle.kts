import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.spring") version Versions.kotlin
    id("org.springframework.boot") version Versions.springBoot
}

apply {
    from("${rootProject.projectDir}/gradle/repositories.gradle.kts")
}


dependencies {
    //
    // BOMs
    //
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${Versions.springBoot}"))
    implementation(platform("org.camunda.bpm:camunda-bom:${Versions.camunda}"))

    //
    // CAMUNDA
    //
    implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp-ee:${Versions.camundaSpringBoot}")
    implementation("com.h2database:h2")

    //
    // KOTLIN
    //
    implementation("io.github.microutils:kotlin-logging:1.6.26")
    implementation(kotlin("stdlib-jdk8"))

    //
    // TEST
    //
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}



tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
