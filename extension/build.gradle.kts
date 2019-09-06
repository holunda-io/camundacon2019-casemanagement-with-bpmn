import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("org.jetbrains.kotlin.plugin.allopen") version Versions.kotlin
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
  // LOGGING
  //
  testImplementation("ch.qos.logback:logback-classic:1.2.3")
  testImplementation("io.github.microutils:kotlin-logging:1.7.6")

  //
  // TEST
  //
  testImplementation("org.camunda.bpm.assert:camunda-bpm-assert:4.0.0")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
  testImplementation("org.assertj:assertj-core:3.13.2")
  testImplementation("org.camunda.bpm.extension.mockito:camunda-bpm-mockito:4.10.0")
  testImplementation("com.h2database:h2:1.4.197")
  testImplementation("com.tngtech.jgiven:jgiven-junit:0.18.1")
}


allOpen {
  annotation("io.holunda.extension.casemanagement._test.JGivenKotlinStage")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
    }
  }
}
