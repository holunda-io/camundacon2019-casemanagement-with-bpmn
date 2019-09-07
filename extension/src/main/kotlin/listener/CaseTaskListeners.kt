package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcess
import io.holunda.extension.casemanagement.bpmn.CaseTaskDefinition
import io.holunda.extension.casemanagement.cmmn.RepetitionRule
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntity
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionProcessVariableRepository
import io.holunda.extension.casemanagement.persistence.CaseTaskDefinitionReadOnlyRepositoryFactory
import io.holunda.extension.casemanagement.sentry.evaluateSentryCondition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener


abstract class CaseTaskListener(val om: ObjectMapper) : ExecutionListener {
  override fun notify(execution: DelegateExecution) = notify(CaseTaskDelegateExecution(om, execution))
  abstract fun notify(delegateExecution: CaseTaskDelegateExecution)
}

class CaseExecutionOnStartListener(
    om: ObjectMapper = jacksonObjectMapper()
) : CaseTaskListener(om) {

  override fun notify(delegateExecution: CaseTaskDelegateExecution) {
    val caseExecutionId = (delegateExecution.delegateExecution.getVariable(CaseProcess.VARIABLES.caseExecutionId) as String).apply {
      delegateExecution.delegateExecution.setVariableLocal(CaseProcess.VARIABLES.caseExecutionId, this)
    }

    with(delegateExecution.repository) {
      val caseExecution = findById(caseExecutionId)!!
          .copy(
              executionId = delegateExecution.id,
              state = BpmnCaseExecutionState.ACTIVE
          )
      save(caseExecution)

      if (delegateExecution.repetitionRule == RepetitionRule.MANUAL_START) {
        save(BpmnCaseExecutionEntity(caseTaskKey = delegateExecution.caseTaskDefinition.key, state = BpmnCaseExecutionState.ENABLED)) // TODO: with sentry, initial state is not enabled!
      }

      commit()
    }
  }

}

class CaseExecutionOnCompleteListener(
    om: ObjectMapper = jacksonObjectMapper()
) : CaseTaskListener(om) {
  override fun notify(delegateExecution: CaseTaskDelegateExecution) {
    with(delegateExecution.repository) {
      save(findById(delegateExecution.caseExecutionId)!!.copy(state = BpmnCaseExecutionState.COMPLETED))

      if (delegateExecution.repetitionRule == RepetitionRule.COMPLETE) {
        val sentryCondition = evaluateSentryCondition(delegateExecution.delegateExecution, delegateExecution.caseTaskDefinition)
        save(BpmnCaseExecutionEntity(
          caseTaskKey = delegateExecution.caseTaskDefinition.key,
          state = if (sentryCondition) BpmnCaseExecutionState.ENABLED else BpmnCaseExecutionState.DISABLED)
        )
      }

      commit()
    }
  }
}

data class CaseTaskDelegateExecution(
    val om: ObjectMapper,
    val delegateExecution: DelegateExecution
) : DelegateExecution by delegateExecution {
  private val definitionRepository = CaseTaskDefinitionReadOnlyRepositoryFactory(om).create(delegateExecution)

  val caseTaskDefinition: CaseTaskDefinition by lazy {
    definitionRepository.findByKey(delegateExecution.currentActivityId)
  }

  val caseExecutionId by lazy { getVariableLocal(CaseProcess.VARIABLES.caseExecutionId) as String }
  val repository: BpmnCaseExecutionProcessVariableRepository = BpmCaseExecutionRepositoryFactory(om).create(delegateExecution)

  val repetitionRule = caseTaskDefinition.repetitionRule
}
