import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
}

dependencies {

  api("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.+")

  implementation(platform("org.camunda.bpm:camunda-bom:${Versions.camunda}"))

  //
  // CAMUNDA
  //
  implementation("org.camunda.bpm:camunda-engine")


  //
  // KOTLIN
  //
  implementation("io.github.microutils:kotlin-logging:1.6.26")
  implementation(kotlin("stdlib-jdk8"))

  //
  // TEST
  //
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation("org.camunda.bpm.assert:camunda-bpm-assert:4.0.0")
  testImplementation("org.assertj:assertj-core:3.13.2")
  testImplementation("org.camunda.bpm.extension.mockito:camunda-bpm-mockito:4.10.0")
  testImplementation("com.h2database:h2:1.4.197")
  testImplementation("org.slf4j:slf4j-simple:1.7.28")
}


tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }
}
