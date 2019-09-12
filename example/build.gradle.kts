import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.spring") version Versions.kotlin
  id("org.springframework.boot") version Versions.springBoot
}

dependencies {
  //
  // BOMs
  //
  implementation(platform("org.springframework.boot:spring-boot-dependencies:${Versions.springBoot}"))
  implementation(platform("org.camunda.bpm:camunda-bom:${Versions.camunda}"))

  implementation(project(":extension"))

  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

  //
  // CAMUNDA
  //
  implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp-ee:${Versions.camundaSpringBoot}")
  implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:${Versions.camundaSpringBoot}")
  implementation("com.h2database:h2")

  //
  // KOTLIN
  //
  implementation("io.github.microutils:kotlin-logging:${Versions.kotlinLogging}")
  implementation(kotlin("stdlib-jdk8"))

  //
  // TEST
  //
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}



tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }
}
