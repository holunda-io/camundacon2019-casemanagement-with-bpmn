package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionQuery
import io.holunda.extension.casemanagement.persistence.CaseTaskDefinitionReadOnlyRepositoryFactory
import io.holunda.extension.casemanagement.sentry.evaluateSentryCondition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate

class ReevaluateSentriesDelegate(private val om: ObjectMapper) : JavaDelegate {

  private val DelegateExecution.caseTaskDefinitionRepository get() = CaseTaskDefinitionReadOnlyRepositoryFactory(om).create(this.processInstance)
  private val DelegateExecution.executionRepository get() = BpmCaseExecutionRepositoryFactory(om).create(this.processInstance)

  override fun execute(execution: DelegateExecution) {
    for (caseTaskDefinition in execution.caseTaskDefinitionRepository.load()) {
      val shouldBeEnabled = evaluateSentryCondition(execution, caseTaskDefinition)

      with(execution.executionRepository) {
        if (shouldBeEnabled) {
          query(BpmnCaseExecutionQuery(caseTaskKey = caseTaskDefinition.key, state = BpmnCaseExecutionState.DISABLED)).forEach {
            save(it.copy(state = BpmnCaseExecutionState.ENABLED))
          }
        } else {
          query(BpmnCaseExecutionQuery(caseTaskKey = caseTaskDefinition.key, state = BpmnCaseExecutionState.ENABLED)).forEach {
            save(it.copy(state = BpmnCaseExecutionState.DISABLED))
          }
        }
        commit()
      }
    }
  }
}
