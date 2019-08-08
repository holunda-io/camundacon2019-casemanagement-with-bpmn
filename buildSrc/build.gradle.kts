plugins {
  `kotlin-dsl`
}

kotlinDslPluginOptions {
  experimentalWarning.set(false)
}

apply {
  // repos set in /gradle
  from("../gradle/repositories.gradle.kts")
}
