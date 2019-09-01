package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.bpmn.parseCaseDefinitions
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntity
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener

/**
 * Listener to be used on the start event of a CaseProcess.
 * It parses the started ProcessInstances BPMN model for subprocesses that should behave like
 * case tasks and stores all data in a process variable.
 */
class CaseProcessStartListener(
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ExecutionListener {

  private val repositoryFactory = BpmCaseExecutionRepositoryFactory(objectMapper)

  override fun notify(execution: DelegateExecution) {
    val caseDefinitions = execution.bpmnModelInstance.parseCaseDefinitions()
    execution.setVariable(CaseProcess.VARIABLES.caseProcessDefinition, objectMapper.writeValueAsString(caseDefinitions))

    val repository = repositoryFactory.create(execution)

    for ((key, definition) in caseDefinitions.tasks) {
      if (!definition.hasSentry && definition.manualStart) {
        repository.save(BpmnCaseExecutionEntity(caseTaskKey = key, state = BpmnCaseExecutionState.ENABLED))
      } else {
        repository.save(BpmnCaseExecutionEntity(key))
      }
    }
    repository.commit()
  }
}
