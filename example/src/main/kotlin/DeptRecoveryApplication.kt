package io.holunda.talk.camundacon

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.listener.ReevaluateSentriesDelegate
import io.holunda.talk.camundacon.process.DeptRecoveryProcessBean
import io.holunda.talk.camundacon.process.DeptRecoveryProcessData
import io.holunda.talk.camundacon.process.StartDeptRecoveryProcessCommand
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

fun main(args: Array<String>) = runApplication<DeptRecoveryApplication>(*args).let { Unit }

@SpringBootApplication
@EnableProcessApplication
class DeptRecoveryApplication {

  @Bean
  fun objectMapper() = jacksonObjectMapper()

  @Bean
  fun reevaluateSentries(om:ObjectMapper) = ReevaluateSentriesDelegate()

}


@Component
class ProcessStarter(val om:ObjectMapper, val process : DeptRecoveryProcessBean) {
  @EventListener
  fun onStart(start: PostDeployEvent) = repeat(1) {
    val initialData = DeptRecoveryProcessData()
    process.start(StartDeptRecoveryProcessCommand(initialData.uuid, om.writeValueAsString(initialData)))
  }
}



typealias Json = String
