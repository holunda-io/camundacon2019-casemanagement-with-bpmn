package io.holunda.extension.casemanagement

import cmmn.BpmnCaseExecutionState
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.holunda.extension.casemanagement.bpmn.CaseProcessDefinition
import io.holunda.extension.casemanagement.persistence.BpmCaseExecutionRepositoryFactory
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntities
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionEntity
import io.holunda.extension.casemanagement.persistence.BpmnCaseExecutionRepository
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.variable.Variables
import java.util.*


interface CaseProcessInstance {

  val repository: BpmnCaseExecutionRepository
  val caseProcessDefinition: CaseProcessDefinition

  val bpmnCaseExecutionEntities: BpmnCaseExecutionEntities

  fun findExecutions(state: BpmnCaseExecutionState? = null, key: String? = null): List<BpmnCaseExecutionEntity>

  fun startManually(caseTaskKey: String): Optional<String>
}

abstract class CaseProcessInstanceWrapper(
    private val processInstance: ProcessInstance,
    private val runtimeService: RuntimeService,
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
) : ProcessInstance by processInstance, CaseProcessInstance {

  override val repository get() = BpmCaseExecutionRepositoryFactory(objectMapper).create(runtimeService, processInstanceId)

  override val caseProcessDefinition
    get() = objectMapper.readValue<CaseProcessDefinition>(runtimeService.getVariable(
        processInstanceId,
        CaseProcess.VARIABLES.caseProcessDefinition) as String
    )

  override val bpmnCaseExecutionEntities
    get() = BpmnCaseExecutionEntities(repository.executions)


  override fun findExecutions(state: BpmnCaseExecutionState?, key: String?): List<BpmnCaseExecutionEntity> {
    val list = if (key != null) repository.findByCaseTaskKey(key) else repository.findAll()
    return if (state != null) list.filter { it.state == state } else list
  }

  override fun startManually(caseTaskKey: String): Optional<String> {
    val enabled = findExecutions(state = BpmnCaseExecutionState.ENABLED, key = caseTaskKey)
    if (enabled.isEmpty()) {
      return Optional.empty()
    }

    val execution = findExecutions(state = BpmnCaseExecutionState.ENABLED, key = caseTaskKey).first()


    runtimeService.createSignalEvent("${caseTaskKey}_$processInstanceId")
        .withoutTenantId()
        .setVariables(Variables.putValue(CaseProcess.VARIABLES.caseExecutionId, execution.id))
        .send()

    return Optional.of(execution.id!!)
  }
}
