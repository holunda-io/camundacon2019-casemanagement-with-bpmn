package io.holunda.talk.camundacon

import io.holunda.extension.casemanagement.jackson.configureObjectMapper
import io.holunda.extension.casemanagement.listener.ReevaluateSentriesDelegate
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

fun main(args: Array<String>) = runApplication<DeptRecoveryApplication>(*args).let { Unit }

@SpringBootApplication
@EnableProcessApplication
class DeptRecoveryApplication {

  @Bean
  fun objectMapper() = configureObjectMapper() // TODO: remove

  @Bean
  fun reevaluateSentries() = ReevaluateSentriesDelegate()

}
