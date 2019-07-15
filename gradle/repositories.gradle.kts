repositories {

  // prefer artifacts from local cache
  mavenLocal()

  // if not found, search on jcenter
  jcenter()

  maven("https://jitpack.io")

  // note: for camunda-ee to work, add  credentials to $HOME/.gradle/gradle.properties
  maven("https://app.camunda.com/nexus/content/repositories/camunda-bpm-ee") {
    name = "camunda-bpm-ee"
    credentials {
      username = properties["camundaRepoUser"] as String
      password = properties["camundaRepoPassword"] as String
    }
  }
}
