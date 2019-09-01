package io.holunda.extension.casemanagement.bpmn

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.camunda.bpm.engine.runtime.CaseExecution
import java.util.*

/**
 * CaseExecution with specific properties and behavior for usage inside a CaseProcess.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BpmnCaseExecution(
    val executionId: String = UUID.randomUUID().toString(),
    val taskDefinition: CaseTaskDefinition,
    val processInstanceId: String,
    val processDefinitionId: String,
    val currentState: BpmnCaseExecutionState = BpmnCaseExecutionState.NEW
) : CaseExecution {

  override fun getId(): String = executionId // TODO: just naming the executionId 'id' caused accidential overwrite error
  override fun isAvailable(): Boolean = BpmnCaseExecutionState.AVAILABLE == currentState
  override fun isEnabled(): Boolean = BpmnCaseExecutionState.ENABLED == currentState
  override fun isActive(): Boolean = BpmnCaseExecutionState.ACTIVE == currentState
  override fun isDisabled(): Boolean = BpmnCaseExecutionState.DISABLED == currentState
  override fun isTerminated(): Boolean = BpmnCaseExecutionState.TERMINATED == currentState

  override fun getActivityId() = taskDefinition.key

  override fun getActivityName() = taskDefinition.name
  override fun getActivityType() = taskDefinition.type.value
  override fun isRequired(): Boolean = taskDefinition.required

  override fun getCaseInstanceId() = processInstanceId
  override fun getCaseDefinitionId() = processDefinitionId

  // things we do not need so far
  override fun getParentId(): String? = null

  override fun getTenantId(): String? = null
  override fun getActivityDescription(): String? = null
}
