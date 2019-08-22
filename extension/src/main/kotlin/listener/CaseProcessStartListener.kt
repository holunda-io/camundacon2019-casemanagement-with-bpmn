package io.holunda.extension.casemanagement.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.bpmn.parseCaseDefinitions
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.slf4j.LoggerFactory

class CaseProcessStartListener(
    val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ExecutionListener {
  override fun notify(execution: DelegateExecution) {
    val caseDefinitions = execution.bpmnModelInstance.parseCaseDefinitions()

    execution.setVariable(CaseProcess.VARIABLES.caseProcessDefinition, objectMapper.writeValueAsString(caseDefinitions))
  }
}
