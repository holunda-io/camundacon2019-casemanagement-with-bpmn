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
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.Variables
import java.util.*


interface CaseProcessInstance {

  val repository: BpmnCaseExecutionRepository
  val caseProcessDefinition: CaseProcessDefinition

  val bpmnCaseExecutionEntities: BpmnCaseExecutionEntities

  fun findExecutions(state: BpmnCaseExecutionState? = null, key: String? = null): List<BpmnCaseExecutionEntity>

  fun startManually(caseTaskKey: String): Optional<String>
  fun startManually(caseTaskKey:CaseTaskKey) = startManually(caseTaskKey.key)

  fun <T:Any> getVariable(key: String) : T?
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

    val execution = enabled.first()
    val variables = Variables.putValueTyped(
            CaseProcess.VARIABLES.caseExecutionId,
            Variables.stringValue(execution.id, true)
    )

    runtimeService.createSignalEvent("${caseTaskKey}_$processInstanceId")
        .withoutTenantId()
        .setVariables(variables)
        .send()

    return Optional.of(execution.id!!)
  }

  override fun <T : Any> getVariable(key: String): T? = runtimeService.getVariable(processInstanceId, key) as T?
}


fun CaseProcessInstanceWrapper.findTask(key:String, taskService: TaskService) : Task? = taskService.createTaskQuery().processInstanceBusinessKey(businessKey).taskDefinitionKey(key).singleResult()
