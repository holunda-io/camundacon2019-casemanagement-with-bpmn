package io.holunda.extension.casemanagement

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.bpmn.BpmnCaseExecutions
import io.holunda.extension.casemanagement.bpmn.CaseProcessDefinition
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntities
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntity
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance


interface CaseProcessInstance {

  val caseProcessDefinition: CaseProcessDefinition

  val bpmnCaseExecutionEntities: BpmnCaseExecutionEntities

  fun findExecutions(state: BpmnCaseExecutionState? = null, key : String? =null ): List<BpmnCaseExecutionEntity>
}

abstract class CaseProcessInstanceWrapper(
    private val processInstance: ProcessInstance,
    private val runtimeService: RuntimeService,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ProcessInstance by processInstance, CaseProcessInstance {

  private val repository = BpmCaseExecutionRepositoryFactory(objectMapper).create(runtimeService, processInstanceId)

  override val caseProcessDefinition
    get() = objectMapper.readValue<CaseProcessDefinition>(runtimeService.getVariable(
        processInstanceId,
        CaseProcess.VARIABLES.caseProcessDefinition) as String
    )

  override val bpmnCaseExecutionEntities
    get() = BpmnCaseExecutionEntities(repository.executions)


  override fun findExecutions(state: BpmnCaseExecutionState?, key: String?): List<BpmnCaseExecutionEntity> {
    val list = if (key != null) repository.findByCaseTaskKey(key) else repository.findAll()
    return if (state != null) list.filter { it.state === state } else list
  }

}
