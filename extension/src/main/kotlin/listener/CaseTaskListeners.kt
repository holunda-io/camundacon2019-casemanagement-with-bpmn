package io.holunda.extension.casemanagement.listener

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.extension.casemanagement.CaseProcessBean
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
    val caseExecutionId = (delegateExecution.delegateExecution.getVariable(CaseProcessBean.VARIABLES.caseExecutionId) as String).apply {
      delegateExecution.delegateExecution.setVariableLocal(CaseProcessBean.VARIABLES.caseExecutionId, this)
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

/**
 * Data class wrapper that extends DelegateExecution with the relevant caseTask meta data.
 */
data class CaseTaskDelegateExecution(
    val om: ObjectMapper,
    val delegateExecution: DelegateExecution
) : DelegateExecution by delegateExecution {
  private val definitionRepository = CaseTaskDefinitionReadOnlyRepositoryFactory().create(delegateExecution)

  val caseTaskDefinition: CaseTaskDefinition by lazy {
    definitionRepository.findByKey(delegateExecution.currentActivityId)
  }

  val caseExecutionId by lazy { getVariableLocal(CaseProcessBean.VARIABLES.caseExecutionId) as String }
  val repository: BpmnCaseExecutionProcessVariableRepository = BpmCaseExecutionRepositoryFactory().create(delegateExecution)

  val repetitionRule = caseTaskDefinition.repetitionRule
}
