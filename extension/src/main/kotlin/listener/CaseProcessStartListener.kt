package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcessBean
import io.holunda.extension.casemanagement.bpmn.parseCaseDefinitions
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntity
import io.holunda.extension.casemanagement.sentry.evaluateSentryCondition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener

/**
 * Listener to be used on the start event of a CaseProcess.
 * It parses the started ProcessInstances BPMN model for subprocesses that should behave like
 * case tasks and stores all data in a process variable.
 */
class CaseProcessStartListener: ExecutionListener {

  private val repositoryFactory = BpmCaseExecutionRepositoryFactory()

  override fun notify(execution: DelegateExecution) {
    val caseProcessDefinition = execution.bpmnModelInstance.parseCaseDefinitions().apply {
      execution.setVariable(
        CaseProcessBean.VARIABLES.caseProcessDefinition,
        this
      )
    }

    val repository = repositoryFactory.create(execution)

    for (caseTaskDefinition in caseProcessDefinition) {
      val sentryCondition = evaluateSentryCondition(execution, caseTaskDefinition)

      val initialState: BpmnCaseExecutionState = when {
        caseTaskDefinition.manualStart && sentryCondition -> BpmnCaseExecutionState.ENABLED
        caseTaskDefinition.manualStart && !sentryCondition -> BpmnCaseExecutionState.DISABLED
        else -> BpmnCaseExecutionState.AVAILABLE
        // TODO: automatic start
        //caseTaskDefinition.automaticStart && sentryCondition -> BpmnCaseExecutionState.ACTIVE
      }

      repository.save(BpmnCaseExecutionEntity(caseTaskKey = caseTaskDefinition.key, state = initialState))
    }

    repository.commit()
  }

}
