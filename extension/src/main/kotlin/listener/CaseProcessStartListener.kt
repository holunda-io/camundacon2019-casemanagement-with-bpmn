package io.holunda.extension.casemanagement.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.bpmn.parseCaseDefinitions
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.slf4j.LoggerFactory

/**
 * Listener to be used on the start event of a CaseProcess.
 * It parses the started ProcessInstances BPMN model for subprocesses that should behave like
 * case tasks and stores all data in a process variable.
 */
class CaseProcessStartListener(
    val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ExecutionListener {
  override fun notify(execution: DelegateExecution) {
    val caseDefinitions = execution.bpmnModelInstance.parseCaseDefinitions()

    execution.setVariable(CaseProcess.VARIABLES.caseProcessDefinition, objectMapper.writeValueAsString(caseDefinitions))
  }
}
